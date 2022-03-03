package dev.momostudios.coldsweat.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class HearthRadiusCapStorage
{
    @Nullable
    public Tag writeNBT(Capability<IBlockStorageCap> capability, IBlockStorageCap instance, Direction direction)
    {
        return new CompoundTag();
    }

    public void readNBT(Capability<IBlockStorageCap> capability, IBlockStorageCap instance, Direction direction, Tag nbtData)
    {
    }
}
