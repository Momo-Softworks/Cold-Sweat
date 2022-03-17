package dev.momostudios.coldsweat.common.capability;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.entity.ModDamageSources;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import dev.momostudios.coldsweat.util.entity.PlayerHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all the information regarding the entity's temperature. This should very rarely be used directly.
 */
@Mod.EventBusSubscriber
public class PlayerTempCapability implements ITemperatureCap
{
    double worldTemp;
    double coreTemp;
    double baseTemp;
    double maxWorldTemp;
    double minWorldTemp;

    List<TempModifier> worldModifiers    = new ArrayList<>();
    List<TempModifier> bodyModifiers     = new ArrayList<>();
    List<TempModifier> baseModifiers     = new ArrayList<>();
    List<TempModifier> rateModifiers     = new ArrayList<>();
    List<TempModifier> maxWorldModifiers = new ArrayList<>();
    List<TempModifier> minWorldModifiers = new ArrayList<>();

    public double get(Temperature.Types type)
    {
        switch (type)
        {
            case WORLD:    return worldTemp;
            case CORE:     return coreTemp;
            case BASE:     return baseTemp;
            case BODY:     return baseTemp + coreTemp;
            case HOTTEST:  return maxWorldTemp;
            case COLDEST:  return minWorldTemp;
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getValue(): " + type);
        }
    }

    public void set(Temperature.Types type, double value)
    {
        switch (type)
        {
            case WORLD   -> this.worldTemp = value;
            case CORE    -> this.coreTemp  = value;
            case BASE    -> this.baseTemp  = value;
            case HOTTEST -> this.maxWorldTemp = value;
            case COLDEST -> this.minWorldTemp = value;
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.setValue(): " + type);
        }
    }

    public List<TempModifier> getModifiers(Temperature.Types type)
    {
        switch (type)
        {
            case WORLD:    { return worldModifiers; }
            case CORE:     { return bodyModifiers; }
            case BASE:     { return baseModifiers; }
            case RATE:     { return rateModifiers; }
            case HOTTEST:  { return maxWorldModifiers; }
            case COLDEST:  { return minWorldModifiers; }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getModifiers(): " + type);
        }
    }

    public boolean hasModifier(Temperature.Types type, Class<? extends TempModifier> mod)
    {
        switch (type)
        {
            case WORLD:    { return this.worldModifiers.stream().anyMatch(mod::isInstance); }
            case CORE:     { return this.bodyModifiers.stream().anyMatch(mod::isInstance); }
            case BASE:     { return this.baseModifiers.stream().anyMatch(mod::isInstance); }
            case RATE:     { return this.rateModifiers.stream().anyMatch(mod::isInstance); }
            case HOTTEST:  { return this.maxWorldModifiers.stream().anyMatch(mod::isInstance); }
            case COLDEST:  { return this.minWorldModifiers.stream().anyMatch(mod::isInstance); }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.hasModifier(): " + type);
        }
    }


    public void clearModifiers(Temperature.Types type)
    {
        switch (type)
        {
            case WORLD ->   this.worldModifiers.clear();
            case CORE ->    this.bodyModifiers.clear();
            case BASE ->    this.baseModifiers.clear();
            case RATE ->    this.rateModifiers.clear();
            case HOTTEST -> this.maxWorldModifiers.clear();
            case COLDEST -> this.minWorldModifiers.clear();
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.clearModifiers(): " + type);
        }
    }

    public void tickClient(Player player)
    {
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.WORLD));
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.CORE));
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.BASE));
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.RATE));
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.HOTTEST));
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.COLDEST));
    }

    public void tickUpdate(Player player)
    {
        ConfigCache config = ConfigCache.getInstance();

        // Tick expiration time for world modifiers
        Temperature world = tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.WORLD));
        double worldTemp = world.get();

        // Apply world temperature modifiers
        set(Temperature.Types.WORLD, world.get());

        Temperature coreTemp = tickModifiers(new Temperature(get(Temperature.Types.CORE)), player, getModifiers(Temperature.Types.CORE));

        Temperature baseTemp = tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.BASE));

        double maxOffset = tickModifiers(new Temperature(maxWorldTemp), player, getModifiers(Temperature.Types.HOTTEST)).get();
        double minOffset = tickModifiers(new Temperature(minWorldTemp), player, getModifiers(Temperature.Types.COLDEST)).get();
        set(Temperature.Types.HOTTEST, maxOffset);
        set(Temperature.Types.COLDEST, minOffset);

        double maxTemp = config.maxTemp + maxOffset;
        double minTemp = config.minTemp + minOffset;

        double tempRate = 7.0d;

        if ((worldTemp > maxTemp && coreTemp.get() >= 0) ||
            (worldTemp < minTemp && coreTemp.get() <= 0))
        {
            boolean isOver = worldTemp > maxTemp;
            double difference = Math.abs(worldTemp - (isOver ? maxTemp : minTemp));
            Temperature changeBy = new Temperature(Math.max((difference / tempRate) * config.rate, Math.abs(config.rate / 50)) * (isOver ? 1 : -1));
            set(Temperature.Types.CORE, coreTemp.add(tickModifiers(changeBy, player, getModifiers(Temperature.Types.RATE))).get());
        }
        else
        {
            // Return the player's body temperature to 0
            Temperature returnRate = new Temperature(getBodyReturnRate(worldTemp, coreTemp.get() > 0 ? maxTemp : minTemp, config.rate, coreTemp.get()));
            set(Temperature.Types.CORE, coreTemp.add(returnRate).get());
        }

        // Sets the player's base temperature
        set(Temperature.Types.BASE, baseTemp.get());

        // Calculate body/base temperatures with modifiers
        Temperature bodyTemp = baseTemp.add(coreTemp);

        if (bodyTemp.get() != get(Temperature.Types.BODY) || player.tickCount % 3 == 0)
        {
            PlayerHelper.updateTemperature(player,
                    new Temperature(get(Temperature.Types.CORE)),
                    new Temperature(get(Temperature.Types.BASE)),
                    new Temperature(get(Temperature.Types.WORLD)),
                    new Temperature(get(Temperature.Types.HOTTEST)),
                    new Temperature(get(Temperature.Types.COLDEST)));
        }

        // Sets the player's body temperature to BASE + CORE
        set(Temperature.Types.CORE, CSMath.clamp(bodyTemp.get(), -150.0 + baseTemp.get(), 150.0 + baseTemp.get()));

        //Deal damage to the player if temperature is critical
        boolean hasFireResistance = player.hasEffect(MobEffects.FIRE_RESISTANCE) && config.fireRes;
        boolean hasIceResistance = player.hasEffect(ModEffects.ICE_RESISTANCE) && config.iceRes;
        if (player.tickCount % 40 == 0)
        {
            boolean damageScaling = config.damageScaling;

            if (bodyTemp.get() >= 100 && !hasFireResistance && !player.hasEffect(ModEffects.GRACE))
            {
                player.hurt(damageScaling ? ModDamageSources.HOT.setScalesWithDifficulty() : ModDamageSources.HOT, 2f);
            }
            if (bodyTemp.get() <= -100 && !hasIceResistance && !player.hasEffect(ModEffects.GRACE))
            {
                player.hurt(damageScaling ? ModDamageSources.COLD.setScalesWithDifficulty() : ModDamageSources.COLD, 2f);
            }
        }
    }

    // Used for returning the player's temperature back to 0
    private static double getBodyReturnRate(double world, double cap, double rate, double bodyTemp)
    {
        double tempRate = 7.0d;
        double changeBy = Math.max((Math.abs(world - cap) / tempRate) * rate, Math.abs(rate / 30));
        return Math.min(Math.abs(bodyTemp), changeBy) * (bodyTemp > 0 ? -1 : 1);
    }

    private static Temperature tickModifiers(Temperature temp, Player player, List<TempModifier> modifiers)
    {
        Temperature result = temp.with(modifiers, player);

        modifiers.removeIf(modifier ->
        {
            if (modifier.getExpireTime() != -1)
            {
                modifier.setTicksExisted(modifier.getTicksExisted() + 1);
                return modifier.getTicksExisted() >= modifier.getExpireTime();
            }
            return false;
        });

        return result;
    }

    public void copy(ITemperatureCap cap)
    {
        for (Temperature.Types type : Temperature.Types.values())
        {
            if (type != Temperature.Types.RATE)
            set(type, cap.get(type));
        }
        for (Temperature.Types type : Temperature.Types.values())
        {
            if (type != Temperature.Types.BODY)
            {
                this.getModifiers(type).clear();
                this.getModifiers(type).addAll(cap.getModifiers(type));
            }
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();

        // Save the player's temperature data
        nbt.putDouble(PlayerHelper.getTempTag(Temperature.Types.CORE), get(Temperature.Types.CORE));
        nbt.putDouble(PlayerHelper.getTempTag(Temperature.Types.BASE), get(Temperature.Types.BASE));
        nbt.putDouble(PlayerHelper.getTempTag(Temperature.Types.HOTTEST), get(Temperature.Types.HOTTEST));
        nbt.putDouble(PlayerHelper.getTempTag(Temperature.Types.COLDEST), get(Temperature.Types.COLDEST));

        // Save the player's modifiers
        Temperature.Types[] validTypes = {Temperature.Types.CORE, Temperature.Types.BASE, Temperature.Types.RATE, Temperature.Types.HOTTEST, Temperature.Types.COLDEST};
        for (Temperature.Types type : validTypes)
        {
            ListTag modifiers = new ListTag();
            for (TempModifier modifier : getModifiers(type))
            {
                modifiers.add(NBTHelper.modifierToTag(modifier));
            }

            // Write the list of modifiers to the player's persistent data
            nbt.put(PlayerHelper.getModifierTag(type), modifiers);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        set(Temperature.Types.CORE, nbt.getDouble(PlayerHelper.getTempTag(Temperature.Types.CORE)));
        set(Temperature.Types.BASE, nbt.getDouble(PlayerHelper.getTempTag(Temperature.Types.BASE)));

        // Load the player's modifiers
        Temperature.Types[] validTypes = {Temperature.Types.CORE, Temperature.Types.BASE, Temperature.Types.RATE};
        for (Temperature.Types type : validTypes)
        {
            // Get the list of modifiers from the player's persistent data
            ListTag modifiers = nbt.getList(PlayerHelper.getModifierTag(type), 10);

            // For each modifier in the list
            modifiers.forEach(modifier ->
            {
                CompoundTag modifierNBT = (CompoundTag) modifier;

                // Add the modifier to the player's temperature
                getModifiers(type).add(NBTHelper.TagToModifier(modifierNBT));
            });
        }
    }
}
