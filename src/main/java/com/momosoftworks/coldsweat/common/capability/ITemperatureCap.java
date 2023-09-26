package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;

import java.util.EnumMap;
import java.util.List;

public interface ITemperatureCap
{
    double getTemp(Temperature.Type type);
    void setTemp(Temperature.Type type, double value);
    List<TempModifier> getModifiers(Temperature.Type type);
    EnumMap<Temperature.Type, Double> getTemperatures();
    boolean hasModifier(Temperature.Type type, Class<? extends TempModifier> mod);
    void clearModifiers(Temperature.Type type);
    void copy(ITemperatureCap cap);
    void tick(LivingEntity entity);
    void tickDummy(LivingEntity entity);
    default void dealTempDamage(LivingEntity target, DamageSource source, float amount)
    {   target.hurt(ConfigSettings.DAMAGE_SCALING.get() ? source.setScalesWithDifficulty() : source, amount);
    }

    CompoundNBT serializeNBT();
    void deserializeNBT(CompoundNBT tag);
    CompoundNBT serializeModifiers();
    void deserializeModifiers(CompoundNBT tag);
    CompoundNBT serializeTemps();
    void deserializeTemps(CompoundNBT tag);
}