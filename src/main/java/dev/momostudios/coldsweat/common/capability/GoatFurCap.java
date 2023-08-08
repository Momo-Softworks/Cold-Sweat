package dev.momostudios.coldsweat.common.capability;

import net.minecraft.nbt.CompoundTag;

public class GoatFurCap implements IShearableCap
{
    boolean sheared = false;
    int lastSheared = 0;

    @Override
    public boolean isSheared()
    {
        return sheared;
    }

    @Override
    public void setSheared(boolean sheared)
    {
        this.sheared = sheared;
    }

    @Override
    public int lastSheared()
    {
        return lastSheared;
    }

    @Override
    public void setLastSheared(int lastSheared)
    {
        this.lastSheared = lastSheared;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("Sheared", sheared);
        nbt.putInt("LastSheared", lastSheared);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        sheared = nbt.getBoolean("Sheared");
        lastSheared = nbt.getInt("LastSheared");
    }
}
