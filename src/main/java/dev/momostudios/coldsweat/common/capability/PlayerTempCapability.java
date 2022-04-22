package dev.momostudios.coldsweat.common.capability;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.entity.ModDamageSources;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import dev.momostudios.coldsweat.util.entity.TempHelper;
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
        return switch (type)
        {
            case WORLD -> worldTemp;
            case CORE  -> coreTemp;
            case BASE  -> baseTemp;
            case BODY  -> baseTemp + coreTemp;
            case MAX   -> maxWorldTemp;
            case MIN   -> minWorldTemp;
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getValue(): " + type);
        };
    }

    public void set(Temperature.Types type, double value)
    {
        switch (type)
        {
            case WORLD -> this.worldTemp = value;
            case CORE  -> this.coreTemp  = value;
            case BASE  -> this.baseTemp  = value;
            case MAX   -> this.maxWorldTemp = value;
            case MIN   -> this.minWorldTemp = value;
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.setValue(): " + type);
        }
    }

    public List<TempModifier> getModifiers(Temperature.Types type)
    {
        return switch (type)
        {
            case WORLD -> worldModifiers;
            case CORE  -> bodyModifiers;
            case BASE  -> baseModifiers;
            case RATE  -> rateModifiers;
            case MAX   -> maxWorldModifiers;
            case MIN   -> minWorldModifiers;
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getModifiers(): " + type);
        };
    }

    public boolean hasModifier(Temperature.Types type, Class<? extends TempModifier> mod)
    {
        return switch (type)
        {
            case WORLD -> this.worldModifiers.stream().anyMatch(mod::isInstance);
            case CORE  -> this.bodyModifiers.stream().anyMatch(mod::isInstance);
            case BASE  -> this.baseModifiers.stream().anyMatch(mod::isInstance);
            case RATE  -> this.rateModifiers.stream().anyMatch(mod::isInstance);
            case MAX   -> this.maxWorldModifiers.stream().anyMatch(mod::isInstance);
            case MIN   -> this.minWorldModifiers.stream().anyMatch(mod::isInstance);
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.hasModifier(): " + type);
        };
    }


    public void clearModifiers(Temperature.Types type)
    {
        switch (type)
        {
            case WORLD -> this.worldModifiers.clear();
            case CORE  -> this.bodyModifiers.clear();
            case BASE  -> this.baseModifiers.clear();
            case RATE  -> this.rateModifiers.clear();
            case MAX   -> this.maxWorldModifiers.clear();
            case MIN   -> this.minWorldModifiers.clear();
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.clearModifiers(): " + type);
        }
    }

    public void tickClient(Player player)
    {
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.WORLD));
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.CORE));
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.BASE));
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.RATE));
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.MAX));
        tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.MIN));
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

        double maxOffset = tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.MAX)).get();
        double minOffset = tickModifiers(new Temperature(), player, getModifiers(Temperature.Types.MIN)).get();
        set(Temperature.Types.MAX, maxOffset);
        set(Temperature.Types.MIN, minOffset);

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

        if ((int) bodyTemp.get() != (int) get(Temperature.Types.BODY) || player.tickCount % 3 == 0)
        {
            TempHelper.updateTemperature(player,
                    new Temperature(get(Temperature.Types.CORE)),
                    new Temperature(get(Temperature.Types.BASE)),
                    new Temperature(get(Temperature.Types.WORLD)),
                    new Temperature(get(Temperature.Types.MAX)),
                    new Temperature(get(Temperature.Types.MIN)));
        }

        // Sets the player's body temperature to BASE + CORE
        if (!CSMath.isBetween(coreTemp.get(), -150, 150))
        {
            set(Temperature.Types.CORE, CSMath.clamp(coreTemp.get(), -150d, 150d));
        }

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
        nbt.putDouble(TempHelper.getTempTag(Temperature.Types.CORE), get(Temperature.Types.CORE));
        nbt.putDouble(TempHelper.getTempTag(Temperature.Types.BASE), get(Temperature.Types.BASE));
        nbt.putDouble(TempHelper.getTempTag(Temperature.Types.MAX), get(Temperature.Types.MAX));
        nbt.putDouble(TempHelper.getTempTag(Temperature.Types.MIN), get(Temperature.Types.MIN));

        // Save the player's modifiers
        Temperature.Types[] validTypes = {Temperature.Types.CORE, Temperature.Types.BASE, Temperature.Types.RATE, Temperature.Types.MAX, Temperature.Types.MIN};
        for (Temperature.Types type : validTypes)
        {
            ListTag modifiers = new ListTag();
            for (TempModifier modifier : getModifiers(type))
            {
                modifiers.add(NBTHelper.modifierToTag(modifier));
            }

            // Write the list of modifiers to the player's persistent data
            nbt.put(TempHelper.getModifierTag(type), modifiers);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        set(Temperature.Types.CORE, nbt.getDouble(TempHelper.getTempTag(Temperature.Types.CORE)));
        set(Temperature.Types.BASE, nbt.getDouble(TempHelper.getTempTag(Temperature.Types.BASE)));

        // Load the player's modifiers
        Temperature.Types[] validTypes = {Temperature.Types.CORE, Temperature.Types.BASE, Temperature.Types.RATE};
        for (Temperature.Types type : validTypes)
        {
            // Get the list of modifiers from the player's persistent data
            ListTag modifiers = nbt.getList(TempHelper.getModifierTag(type), 10);

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
