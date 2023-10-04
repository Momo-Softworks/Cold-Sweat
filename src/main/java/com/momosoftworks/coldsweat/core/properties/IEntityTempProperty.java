package com.momosoftworks.coldsweat.core.properties;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.EnumMap;
import java.util.List;

public interface IEntityTempProperty extends IExtendedEntityProperties
{
    double getTemp(Temperature.Type type);
    void setTemp(Temperature.Type type, double value);
    List<TempModifier> getModifiers(Temperature.Type type);
    EnumMap<Temperature.Type, Double> getTemperatures();
    boolean hasModifier(Temperature.Type type, Class<? extends TempModifier> mod);
    void clearModifiers(Temperature.Type type);
    void copy(IEntityTempProperty cap);
    void tick(EntityLivingBase entity);
    void tickDummy(EntityLivingBase entity);
    default void dealTempDamage(EntityLivingBase target, DamageSource source, float amount)
    {   target.attackEntityFrom(ConfigSettings.DAMAGE_SCALING.get() ? source.setDifficultyScaled() : source, amount);
    }

    void saveNBTData(NBTTagCompound nbt);
    void loadNBTData(NBTTagCompound nbt);

    NBTTagCompound serializeModifiers();
    void deserializeModifiers(NBTTagCompound tag);
    NBTTagCompound serializeTemps();
    void deserializeTemps(NBTTagCompound tag);
}