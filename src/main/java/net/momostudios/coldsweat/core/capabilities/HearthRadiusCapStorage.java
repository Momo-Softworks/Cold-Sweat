package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.nbt.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.momostudios.coldsweat.util.SpreadPath;

import javax.annotation.Nullable;

public class HearthRadiusCapStorage implements Capability.IStorage<IBlockStorageCap>
{
    @Override
    @Nullable
    public INBT writeNBT(Capability<IBlockStorageCap> capability, IBlockStorageCap instance, Direction direction)
    {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT list = new ListNBT();

        for (SpreadPath pos : instance.getHashSet())
        {
            list.add(LongNBT.valueOf(pos.getPos().toLong()));
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
            instance.add(new SpreadPath(BlockPos.fromLong(((LongNBT) inbt).getLong())));
        }
    }
}
