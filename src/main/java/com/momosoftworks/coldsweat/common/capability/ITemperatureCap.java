package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public interface ITemperatureCap
{
    double getTemp(Temperature.Type type);
    double getAbility(Temperature.Ability type);
    void setTemp(Temperature.Type type, double value);
    void setAbility(Temperature.Ability type, double value);
    List<TempModifier> getModifiers(Temperature.Type type);
    EnumMap<Temperature.Type, Double> getTemperatures();
    boolean hasModifier(Temperature.Type type, Class<? extends TempModifier> mod);
    void addModifier(TempModifier modifier, Temperature.Type type);
    void clearModifiers(Temperature.Type type);
    void markPersistentAttribute(Attribute attribute);
    void clearPersistentAttribute(Attribute attribute);
    Collection<Attribute> getPersistentAttributes();
    void copy(ITemperatureCap cap);
    void tick(LivingEntity entity);
    void tickDummy(LivingEntity entity);

    Temperature.Units getPreferredUnits();
    void setPreferredUnits(Temperature.Units units);

    CompoundTag serializeNBT();
    CompoundTag serializeModifiers();
    CompoundTag serializeTemps();
    CompoundTag serializeAbilities();
    void deserializeNBT(CompoundTag tag);
    void deserializeModifiers(CompoundTag tag);
    void deserializeTemps(CompoundTag tag);
    void deserializeAbilities(CompoundTag tag);
}