package dev.momostudios.coldsweat.common.capability;

import net.minecraft.nbt.CompoundNBT;

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
    public CompoundNBT serializeNBT()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("Sheared", sheared);
        nbt.putInt("LastSheared", lastSheared);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        sheared = nbt.getBoolean("Sheared");
        lastSheared = nbt.getInt("LastSheared");
    }
}
