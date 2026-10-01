package com.momosoftworks.coldsweat.common.capability.shearing;

import net.minecraft.nbt.CompoundTag;
import com.momosoftworks.coldsweat.common.capability.CompoundSerializable;

public interface IShearableCap extends CompoundSerializable
{
    boolean isSheared();
    void setSheared(boolean sheared);

    int furGrowthCooldown();
    void setFurGrowthCooldown(int cooldown);

    int age();
    void setAge(int ticks);
}
