package dev.momostudios.coldsweat.core.capabilities;

import net.minecraft.nbt.*;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class HearthRadiusCapStorage implements Capability.IStorage<IBlockStorageCap>
{
    @Override
    @Nullable
    public INBT writeNBT(Capability<IBlockStorageCap> capability, IBlockStorageCap instance, Direction direction)
    {
        return new CompoundNBT();
    }

    @Override
    public void readNBT(Capability<IBlockStorageCap> capability, IBlockStorageCap instance, Direction direction, INBT nbtData)
    {
    }
}
