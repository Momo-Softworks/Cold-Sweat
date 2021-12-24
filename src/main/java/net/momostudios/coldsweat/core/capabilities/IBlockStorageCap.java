package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.List;

public interface IBlockStorageCap
{
    HashSet<BlockPos> getHashSet();

    void setHashSet(HashSet<BlockPos> list);

    void add(BlockPos pos);

    void addAll (List<BlockPos> posList);

    void remove(BlockPos pos);

    void clear();
}
