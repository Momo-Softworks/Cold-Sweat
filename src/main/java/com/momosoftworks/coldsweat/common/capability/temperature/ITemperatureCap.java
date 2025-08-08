package com.momosoftworks.coldsweat.common.capability.temperature;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public interface ITemperatureCap extends INBTSerializable<CompoundTag>
{
    double getTrait(Temperature.Trait trait);
    EnumMap<Temperature.Trait, Double> getTraits();
    void setTrait(Temperature.Trait trait, double value);

    EnumMap<Temperature.Trait, List<TempModifier>> getModifiers();
    List<TempModifier> getModifiers(Temperature.Trait trait);
    void addModifier(TempModifier modifier, Temperature.Trait trait);
    void removeModifier(TempModifier modifier, Temperature.Trait trait);

    boolean hasModifier(Temperature.Trait trait, Class<? extends TempModifier> mod);
    void setModifiers(Map<Temperature.Trait, List<TempModifier>> modifiers);
    void clearModifiers(Temperature.Trait trait);
    void clearModifiers();

    void markPersistentAttribute(Attribute attribute);
    void clearPersistentAttribute(Attribute attribute);
    Collection<Attribute> getPersistentAttributes();

    void addTempEffect(TempEffect effect);
    void removeTempEffect(TempEffectType<?> effect);
    void clearTempEffects();
    Map<TempEffectType<?>, TempEffect> getTempEffects();

    void tick(LivingEntity entity);
    void tickDummy(LivingEntity entity);

    void copy(ITemperatureCap cap);
    void syncValues(LivingEntity entity);

    CompoundTag serializeModifiers();
    CompoundTag serializeTraits();
    void deserializeModifiers(CompoundTag tag);
    void deserializeTraits(CompoundTag tag);
}