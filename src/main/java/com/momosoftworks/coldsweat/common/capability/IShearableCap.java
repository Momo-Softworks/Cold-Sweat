package com.momosoftworks.coldsweat.common.capability;

import net.minecraft.nbt.CompoundNBT;

public interface IShearableCap
{
    boolean isSheared();
    void setSheared(boolean sheared);
    int lastSheared();
    void setLastSheared(int lastSheared);

    CompoundNBT serializeNBT();

    void deserializeNBT(CompoundNBT nbt);
}
