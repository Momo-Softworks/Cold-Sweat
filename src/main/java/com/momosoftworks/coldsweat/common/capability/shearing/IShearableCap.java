package com.momosoftworks.coldsweat.common.capability.shearing;

import net.minecraft.nbt.CompoundTag;

public interface IShearableCap
{
    boolean isSheared();
    void setSheared(boolean sheared);
    int lastSheared();
    void setLastSheared(int lastSheared);

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag nbt);
}
