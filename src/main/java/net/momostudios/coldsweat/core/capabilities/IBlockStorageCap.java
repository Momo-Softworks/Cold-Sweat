package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.util.SpreadPath;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public interface IBlockStorageCap
{
    @Nonnull
    ConcurrentHashMap<BlockPos, SpreadPath> getMap();

    void setPaths(ConcurrentHashMap<BlockPos, SpreadPath> list);

    void set(SpreadPath pos);

    void addPaths(ConcurrentHashMap<BlockPos, SpreadPath> posList);

    void remove(SpreadPath pos);

    void clear();
}
