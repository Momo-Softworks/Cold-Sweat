package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.util.SpreadPath;

import java.util.*;

public interface IBlockStorageCap
{
    LinkedHashMap<BlockPos, SpreadPath> getMap();

    void setPaths(LinkedHashMap<BlockPos, SpreadPath> list);

    void set(SpreadPath pos);

    void addPaths(LinkedHashMap<BlockPos, SpreadPath> posList);

    void remove(SpreadPath pos);

    void clear();
}
