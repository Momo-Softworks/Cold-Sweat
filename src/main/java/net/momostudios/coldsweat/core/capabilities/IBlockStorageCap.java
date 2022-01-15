package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.core.util.SpreadPath;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public interface IBlockStorageCap
{
    HashSet<SpreadPath> getHashSet();
    HashSet<BlockPos> getPositions();

    void setPaths(HashSet<SpreadPath> list);

    void add(SpreadPath pos);

    void addPaths(Collection<SpreadPath> posList);

    void addPoints(Collection<BlockPos> posList);

    void remove(SpreadPath pos);

    void clear();
}
