package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Temperature.Type;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import static com.momosoftworks.coldsweat.common.capability.EntityTempManager.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

/**
 * Holds all the information regarding the entity's temperature. <br>
 * This capability isn't used for players (see {@link PlayerTempCap} instead).
 */
public class EntityTempCap implements ITemperatureCap
{
    private double[] syncedValues = new double[5];
    boolean neverSynced = true;
    // Ensures a minimum time between syncs
    int syncTimer = 0;

    // Map valid temperature types to a new EnumMap
    EnumMap<Type, Double> temperatures = Arrays.stream(VALID_MODIFIER_TYPES).collect(
            () -> new EnumMap<>(Type.class),
            (map, type) -> map.put(type, 0.0),
            EnumMap::putAll);

    // Map valid modifier types to a new EnumMap
    EnumMap<Type, List<TempModifier>> modifiers = Arrays.stream(VALID_MODIFIER_TYPES).collect(
            () -> new EnumMap<>(Type.class),
            (map, type) -> map.put(type, new ArrayList<>()),
            EnumMap::putAll);

    public double getTemp(Type type)
    {
        // Special case for BODY
        if (type == Type.BODY) return getTemp(Type.CORE) + getTemp(Type.BASE);
        // Throw exception if this temperature type is not supported
        return temperatures.computeIfAbsent(type, t ->
        {   throw new IllegalArgumentException("Invalid temperature type: " + t);
        });
    }

    @Override
    public EnumMap<Type, Double> getTemperatures()
    {   return new EnumMap<>(temperatures);
    }

    public void setTemp(Type type, double value)
    {
        // Throw exception if this temperature type is not supported
        if (temperatures.replace(type, value) == null)
        {   throw new IllegalArgumentException("Invalid temperature type: " + type);
        }
    }

    public List<TempModifier> getModifiers(Type type)
    {
        // Throw exception if this modifier type is not supported
        return modifiers.computeIfAbsent(type, t ->
        {   throw new IllegalArgumentException("Invalid modifier type: " + t);
        });
    }

    public boolean hasModifier(Type type, Class<? extends TempModifier> mod)
    {
        return getModifiers(type).stream().anyMatch(mod::isInstance);
    }

    public void clearModifiers(Type type)
    {
        getModifiers(type).clear();
    }

    // See Temperature.class for more temperature-related methods

    /**
     * Used for clientside ticking of TempModifiers. The result is not used.
     */
    public void tickDummy(LivingEntity entity)
    {
        Temperature.apply(0, entity, Type.WORLD, getModifiers(Type.WORLD));
        Temperature.apply(getTemp(Type.CORE), entity, Type.CORE, getModifiers(Type.CORE));
        Temperature.apply(0, entity, Type.BASE, getModifiers(Type.BASE));
        Temperature.apply(0, entity, Type.FREEZING_POINT, getModifiers(Type.FREEZING_POINT));
        Temperature.apply(0, entity, Type.BURNING_POINT, getModifiers(Type.BURNING_POINT));
    }

    public void tick(LivingEntity entity)
    {
        // Tick expiration time for world modifiers
        double newWorldTemp = Temperature.apply(0, entity, Type.WORLD, getModifiers(Type.WORLD));
        double newCoreTemp  = Temperature.apply(getTemp(Type.CORE), entity, Type.CORE, getModifiers(Type.CORE));
        double newBaseTemp  = Temperature.apply(0, entity, Type.BASE, getModifiers(Type.BASE));
        double newMaxOffset = Temperature.apply(0, entity, Type.FREEZING_POINT, getModifiers(Type.FREEZING_POINT));
        double newMinOffset = Temperature.apply(0, entity, Type.BURNING_POINT, getModifiers(Type.BURNING_POINT));

        double maxTemp = ConfigSettings.MAX_TEMP.get() + newMaxOffset;
        double minTemp = ConfigSettings.MIN_TEMP.get() + newMinOffset;

        // 1 if newWorldTemp is above max, -1 if below min, 0 if between the values (safe)
        int magnitude = CSMath.getSignForRange(newWorldTemp, minTemp, maxTemp);

        // If temp is dangerous, change core temp
        if (magnitude != 0)
        {
            double difference = Math.abs(newWorldTemp - CSMath.clamp(newWorldTemp, minTemp, maxTemp));
            double changeBy = Math.max((difference / 7d) * ConfigSettings.TEMP_RATE.get().floatValue(), Math.abs(ConfigSettings.TEMP_RATE.get().floatValue() / 50d)) * magnitude;
            newCoreTemp += Temperature.apply(changeBy, entity, Type.RATE, getModifiers(Type.RATE));
        }
        // If the entity's temperature and world temperature are not both hot or both cold, return to neutral
        int tempSign = CSMath.getSign(newCoreTemp);
        if (tempSign != 0 && magnitude != tempSign && getModifiers(Type.CORE).isEmpty())
        {
            double factor = (tempSign == 1 ? newWorldTemp - maxTemp : newWorldTemp - minTemp) / 3;
            double changeBy = CSMath.maxAbs(factor * ConfigSettings.TEMP_RATE.get(), ConfigSettings.TEMP_RATE.get() / 10d * -tempSign);
            newCoreTemp += CSMath.minAbs(changeBy, -getTemp(Type.CORE));
        }

        // Write the new temperature values
        setTemp(Type.BASE, newBaseTemp);
        setTemp(Type.CORE, CSMath.clamp(newCoreTemp, -150f, 150f));
        setTemp(Type.WORLD, newWorldTemp);
        setTemp(Type.FREEZING_POINT, newMaxOffset);
        setTemp(Type.BURNING_POINT, newMinOffset);

        if (syncTimer > 0)
            syncTimer--;

        // Sync the temperature values to the client
        if (syncTimer <= 0 || (neverSynced
        || (int) syncedValues[0] != (int) newCoreTemp
        || (int) syncedValues[1] != (int) newBaseTemp
        || Math.abs(syncedValues[2] - newWorldTemp) >= 0.02
        || Math.abs(syncedValues[3] - newMaxOffset) >= 0.02
        || Math.abs(syncedValues[4] - newMinOffset) >= 0.02))
        {
            Temperature.updateTemperature(entity, this, false);
            syncedValues = new double[] { newCoreTemp, newBaseTemp, newWorldTemp, newMaxOffset, newMinOffset };
            neverSynced = false;
            syncTimer = 40;
        }

        // Don't natively deal temperature damage to entities
        /*
        // Calculate body/base temperatures with modifiers
        double bodyTemp = getTemp(Type.BODY);

        //Deal damage to the player if temperature is critical
        if (!entity.isCreative() && !entity.isSpectator())
        {
            if (entity.tickCount % 40 == 0 && !entity.hasEffect(ModEffects.GRACE))
            {
                boolean damageScaling = config.damageScaling;

                if (bodyTemp >= 100 && !(entity.hasEffect(MobEffects.FIRE_RESISTANCE) && config.fireRes))
                {
                    entity.hurt(damageScaling ? ModDamageSources.HOT.setScalesWithDifficulty() : ModDamageSources.HOT, 2f);
                }
                else if (bodyTemp <= -100 && !(entity.hasEffect(ModEffects.ICE_RESISTANCE) && config.iceRes))
                {
                    entity.hurt(damageScaling ? ModDamageSources.COLD.setScalesWithDifficulty() : ModDamageSources.COLD, 2f);
                }
            }
        }
        else setTemp(Type.CORE, 0);
        */
    }

    @Override
    public void copy(ITemperatureCap cap)
    {
        // Copy temperature values
        for (Type type : VALID_TEMPERATURE_TYPES)
        {
            if (type == Type.BODY || type == Type.RATE) continue;
            this.setTemp(type, cap.getTemp(type));
        }

        // Copy the modifiers
        for (Type type : VALID_MODIFIER_TYPES)
        {
            this.getModifiers(type).clear();
            this.getModifiers(type).addAll(cap.getModifiers(type));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        // Save the player's temperatures
        CompoundTag nbt = this.serializeTemps();

        // Save the player's modifiers
        nbt.merge(this.serializeModifiers());
        return nbt;
    }

    @Override
    public CompoundTag serializeTemps()
    {
        CompoundTag nbt = new CompoundTag();

        // Save the player's temperature data
        for (Type type : VALID_TEMPERATURE_TYPES)
        {
            nbt.putDouble(NBTHelper.getTemperatureTag(type), this.getTemp(type));
        }
        return nbt;
    }

    @Override
    public CompoundTag serializeModifiers()
    {
        CompoundTag nbt = new CompoundTag();

        // Save the player's modifiers
        for (Type type : VALID_MODIFIER_TYPES)
        {
            ListTag modifiers = new ListTag();
            for (TempModifier modifier : this.getModifiers(type))
            {
                modifiers.add(NBTHelper.modifierToTag(modifier));
            }

            // Write the list of modifiers to the player's persistent data
            nbt.put(NBTHelper.getModifierTag(type), modifiers);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        // Load the player's temperatures
        deserializeTemps(nbt);

        // Load the player's modifiers
        deserializeModifiers(nbt);
    }

    @Override
    public void deserializeTemps(CompoundTag nbt)
    {
        for (Type type : VALID_TEMPERATURE_TYPES)
        {
            setTemp(type, nbt.getDouble(NBTHelper.getTemperatureTag(type)));
        }
    }

    @Override
    public void deserializeModifiers(CompoundTag nbt)
    {
        for (Type type : VALID_MODIFIER_TYPES)
        {
            getModifiers(type).clear();

            // Get the list of modifiers from the player's persistent data
            ListTag modifiers = nbt.getList(NBTHelper.getModifierTag(type), 10);

            // For each modifier in the list
            modifiers.forEach(modNBT ->
            {
                NBTHelper.tagToModifier((CompoundTag) modNBT).ifPresent(modifier ->
                {   getModifiers(type).add(modifier);
                });
            });
        }
    }
}
