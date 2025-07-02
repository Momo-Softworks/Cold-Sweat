package com.momosoftworks.coldsweat.common.capability.shearing;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface IShearableCap extends INBTSerializable<CompoundTag>
{
    boolean isSheared();
    void setSheared(boolean sheared);

    int furGrowthCooldown();
    void setFurGrowthCooldown(int cooldown);

    int age();
    void setAge(int ticks);
}
