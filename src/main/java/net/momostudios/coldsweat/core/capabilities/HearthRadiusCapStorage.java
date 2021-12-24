package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.nbt.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HearthRadiusCapStorage implements Capability.IStorage<IBlockStorageCap>
{
    @Override
    @Nullable
    public INBT writeNBT(Capability<IBlockStorageCap> capability, IBlockStorageCap instance, Direction direction)
    {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT list = new ListNBT();

        for (BlockPos pos : instance.getHashSet())
        {
            list.add(LongNBT.valueOf(pos.toLong()));
        }
        nbt.put("points", list);
        return nbt;
    }

    @Override
    public void readNBT(Capability<IBlockStorageCap> capability, IBlockStorageCap instance, Direction direction, INBT nbtData)
    {
        CompoundNBT nbt = (CompoundNBT) nbtData;

        instance.clear();
        for (INBT inbt : nbt.getList("points", 11))
        {
            instance.add(BlockPos.fromLong(((LongNBT) inbt).getLong()));
        }
    }
}
