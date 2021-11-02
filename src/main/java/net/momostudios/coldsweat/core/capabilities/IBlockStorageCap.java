package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface IBlockStorageCap
{
    List<BlockPos> getList();

    void setList(List<BlockPos> list);

    void add(BlockPos pos);

    void addAll (List<BlockPos> posList);

    void remove(BlockPos pos);

    void clear();
}
