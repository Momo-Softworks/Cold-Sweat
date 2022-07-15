package dev.momostudios.coldsweat.common.capability;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.PlayerTempSyncMessage;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.util.entity.ModDamageSources;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import dev.momostudios.coldsweat.util.entity.TempHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds all the information regarding the entity's temperature. This should very rarely be used directly.
 */
public class PlayerTempCap implements ITemperatureCap
{
    static Temperature.Types[] VALID_MODIFIER_TYPES = {Temperature.Types.CORE, Temperature.Types.BASE, Temperature.Types.RATE,
                                                       Temperature.Types.MAX,  Temperature.Types.MIN,  Temperature.Types.WORLD};

    private double[] syncedValues = new double[5];
    private int ticksSinceSync = 0;

    double worldTemp;
    double coreTemp;
    double baseTemp;
    double maxTemp;
    double minTemp;

    List<TempModifier> worldModifiers    = new ArrayList<>();
    List<TempModifier> coreModifiers     = new ArrayList<>();
    List<TempModifier> baseModifiers     = new ArrayList<>();
    List<TempModifier> rateModifiers     = new ArrayList<>();
    List<TempModifier> maxModifiers = new ArrayList<>();
    List<TempModifier> minModifiers = new ArrayList<>();

    public double get(Temperature.Types type)
    {
        return switch (type)
        {
            case WORLD -> worldTemp;
            case CORE  -> coreTemp;
            case BASE  -> baseTemp;
            case BODY  -> baseTemp + coreTemp;
            case MAX   -> maxTemp;
            case MIN   -> minTemp;
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
            case MAX   -> this.maxTemp = value;
            case MIN   -> this.minTemp = value;
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.setValue(): " + type);
        }
    }

    public List<TempModifier> getModifiers(Temperature.Types type)
    {
        return switch (type)
        {
            case WORLD -> worldModifiers;
            case CORE  -> coreModifiers;
            case BASE  -> baseModifiers;
            case RATE  -> rateModifiers;
            case MAX   -> maxModifiers;
            case MIN   -> minModifiers;
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getModifiers(): " + type);
        };
    }

    public boolean hasModifier(Temperature.Types type, Class<? extends TempModifier> mod)
    {
        return switch (type)
        {
            case WORLD -> this.worldModifiers.stream().anyMatch(mod::isInstance);
            case CORE  -> this.coreModifiers.stream().anyMatch(mod::isInstance);
            case BASE  -> this.baseModifiers.stream().anyMatch(mod::isInstance);
            case RATE  -> this.rateModifiers.stream().anyMatch(mod::isInstance);
            case MAX   -> this.maxModifiers.stream().anyMatch(mod::isInstance);
            case MIN   -> this.minModifiers.stream().anyMatch(mod::isInstance);
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.hasModifier(): " + type);
        };
    }


    public void clearModifiers(Temperature.Types type)
    {
        switch (type)
        {
            case WORLD -> this.worldModifiers.clear();
            case CORE  -> this.coreModifiers.clear();
            case BASE  -> this.baseModifiers.clear();
            case RATE  -> this.rateModifiers.clear();
            case MAX   -> this.maxModifiers.clear();
            case MIN   -> this.minModifiers.clear();
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.clearModifiers(): " + type);
        }
    }

    @Override
    public void copy(ITemperatureCap cap)
    {
        this.worldModifiers = cap.getModifiers(Temperature.Types.WORLD);
        this.baseModifiers  = cap.getModifiers(Temperature.Types.BASE);
        this.coreModifiers  = cap.getModifiers(Temperature.Types.CORE);
        this.rateModifiers  = cap.getModifiers(Temperature.Types.RATE);
        this.maxModifiers   = cap.getModifiers(Temperature.Types.RATE);
        this.minModifiers   = cap.getModifiers(Temperature.Types.RATE);

        this.worldTemp = cap.get(Temperature.Types.WORLD);
        this.coreTemp = cap.get(Temperature.Types.CORE);
        this.baseTemp = cap.get(Temperature.Types.BASE);
        this.maxTemp = cap.get(Temperature.Types.MAX);
        this.minTemp = cap.get(Temperature.Types.MIN);
    }

    public void tickDummy(Player player)
    {
        for (Temperature.Types type : VALID_MODIFIER_TYPES)
        {
            tickModifiers(player, new Temperature(), type);
        }
    }

    public void tick(Player player)
    {
        ConfigCache config = ConfigCache.getInstance();

        // Tick expiration time for world modifiers
        double worldTemp = tickModifiers(player, new Temperature(), Temperature.Types.WORLD).get();
        Temperature coreTemp = tickModifiers(player, new Temperature(this.coreTemp), Temperature.Types.CORE);
        Temperature baseTemp = tickModifiers(player, new Temperature(), Temperature.Types.BASE);
        double maxOffset = tickModifiers(player, new Temperature(), Temperature.Types.MAX).get();
        double minOffset = tickModifiers(player, new Temperature(), Temperature.Types.MIN).get();

        double maxTemp = config.maxTemp + maxOffset;
        double minTemp = config.minTemp + minOffset;

        double tempRate = 7.0d;

        if (worldTemp > maxTemp || worldTemp < minTemp)
        {
            boolean isOver = worldTemp > maxTemp;
            double difference = Math.abs(worldTemp - (isOver ? maxTemp : minTemp));
            Temperature changeBy = new Temperature(Math.max((difference / tempRate) * config.rate, Math.abs(config.rate / 50)) * (isOver ? 1 : -1));
            coreTemp = coreTemp.add(tickModifiers(player, changeBy, Temperature.Types.RATE));
        }
        // Return the player's body temperature to 0
        coreTemp = coreTemp.add(getBodyReturnRate(worldTemp, coreTemp.get() > 0 ? maxTemp : minTemp, config.rate, coreTemp.get()));

        if (ticksSinceSync++ >= 5
        && ((int) syncedValues[0] != (int) coreTemp.get()
        || (int) syncedValues[1] != (int) baseTemp.get()
        || CSMath.crop(syncedValues[2], 2) != CSMath.crop(worldTemp, 2)
        || CSMath.crop(syncedValues[3], 2) != CSMath.crop(maxOffset, 2)
        || CSMath.crop(syncedValues[4], 2) != CSMath.crop(minOffset, 2)))
        {
            ticksSinceSync = 0;
            syncedValues = new double[] { coreTemp.get(), baseTemp.get(), worldTemp, maxOffset, minOffset };

            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new PlayerTempSyncMessage(
                            worldTemp,
                            coreTemp.get(),
                            baseTemp.get(),
                            maxOffset,
                            minOffset, false));
        }

        // Sets the player's body temperature to BASE + CORE
        set(Temperature.Types.BASE, baseTemp.get());
        set(Temperature.Types.CORE, CSMath.clamp(coreTemp.get(), -150d, 150d));
        set(Temperature.Types.WORLD, worldTemp);
        set(Temperature.Types.MAX, maxOffset);
        set(Temperature.Types.MIN, minOffset);

        // Calculate body/base temperatures with modifiers
        Temperature bodyTemp = baseTemp.add(coreTemp);

        boolean hasFireResistance = player.hasEffect(MobEffects.FIRE_RESISTANCE) && config.fireRes;
        boolean hasIceResistance = player.hasEffect(ModEffects.ICE_RESISTANCE)   && config.iceRes;

        //Deal damage to the player if temperature is critical
        if (player.tickCount % 40 == 0)
        {
            boolean damageScaling = config.damageScaling;

            if (bodyTemp.get() >= 100 && !hasFireResistance && !player.hasEffect(ModEffects.GRACE))
            {
                player.hurt(damageScaling ? ModDamageSources.HOT.setScalesWithDifficulty() : ModDamageSources.HOT, 2f);
            }
            else if (bodyTemp.get() <= -100 && !hasIceResistance && !player.hasEffect(ModEffects.GRACE))
            {
                player.hurt(damageScaling ? ModDamageSources.COLD.setScalesWithDifficulty() : ModDamageSources.COLD, 2f);
            }
        }
    }

    // Used for returning the player's temperature back to 0
    private double getBodyReturnRate(double worldTemp, double tempLimit, double tempRate, double bodyTemp)
    {
        double staticRate = 3.0d;
        // Get the difference between the world temp and the threshold to determine the speed of return (closer to the threshold = slower)
        // Divide it by the staticRate (7) because it feels nice
        // Multiply it by the configured tempRate Rate Modifier
        // If it's too slow, default to tempRate / 30 instead
        // Multiply it by -CSMath.getSign(bodyTemp) to make it go toward 0
        double changeBy = Math.max((Math.abs(worldTemp - tempLimit) / staticRate) * tempRate, tempRate / 10) * -CSMath.getSign(bodyTemp);
        return CSMath.getLeastExtreme(changeBy, -bodyTemp);
    }

    private Temperature tickModifiers(Player player, Temperature temp, Temperature.Types type)
    {
        List<TempModifier> modifiers = getModifiers(type);
        AtomicReference<Temperature> newTemp = new AtomicReference<>(temp);

        modifiers.removeIf(modifier ->
        {
            // Apply the TempModifier's function to the Temperature
            // If the modifier's tick rate lines up, calculate the new function
            newTemp.set(player.tickCount % modifier.getTickRate() == 0 || modifier.getTicksExisted() == 0
                    ? newTemp.get().with(modifier, player)
                    : modifier.getFunction().apply(newTemp.get()));

            modifier.setTicksExisted(modifier.getTicksExisted() + 1);
            return modifier.getTicksExisted() > modifier.getExpireTime() && modifier.getExpireTime() != -1;
        });

        return newTemp.get();
    }

    public void copy(PlayerTempCap cap)
    {
        // Copy temperature values
        for (Temperature.Types type : Temperature.Types.values())
        {
            if (type == Temperature.Types.BODY || type == Temperature.Types.RATE) continue;
            this.set(type, cap.get(type));
        }

        // Copy the modifiers
        for (Temperature.Types type : VALID_MODIFIER_TYPES)
        {
            this.getModifiers(type).clear();
            this.getModifiers(type).addAll(cap.getModifiers(type));
        }
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();

        // Save the player's temperature data
        nbt.putDouble(TempHelper.getTempTag(Temperature.Types.CORE), get(Temperature.Types.CORE));
        nbt.putDouble(TempHelper.getTempTag(Temperature.Types.BASE), get(Temperature.Types.BASE));
        nbt.putDouble(TempHelper.getTempTag(Temperature.Types.MAX), get(Temperature.Types.MAX));
        nbt.putDouble(TempHelper.getTempTag(Temperature.Types.MIN), get(Temperature.Types.MIN));

        // Save the player's modifiers
        for (Temperature.Types type : VALID_MODIFIER_TYPES)
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

    public void deserializeNBT(CompoundTag nbt)
    {
        set(Temperature.Types.CORE, nbt.getDouble(TempHelper.getTempTag(Temperature.Types.CORE)));
        set(Temperature.Types.BASE, nbt.getDouble(TempHelper.getTempTag(Temperature.Types.BASE)));

        // Load the player's modifiers
        for (Temperature.Types type : VALID_MODIFIER_TYPES)
        {
            // Get the list of modifiers from the player's persistent data
            ListTag modifiers = nbt.getList(TempHelper.getModifierTag(type), 10);

            // For each modifier in the list
            modifiers.forEach(modifier ->
            {
                CompoundTag modifierNBT = (CompoundTag) modifier;

                // Add the modifier to the player's temperature
                getModifiers(type).add(NBTHelper.tagToModifier(modifierNBT));
            });
        }
    }
}
