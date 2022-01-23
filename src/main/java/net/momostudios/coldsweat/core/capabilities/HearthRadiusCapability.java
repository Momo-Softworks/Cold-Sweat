package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.momostudios.coldsweat.util.SpreadPath;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class HearthRadiusCapability implements IBlockStorageCap
{
    public static Capability<IBlockStorageCap> HEARTH_BLOCKS;

    @CapabilityInject(IBlockStorageCap.class)
    private static void onCapInit(Capability<IBlockStorageCap> capability)
    {
        HEARTH_BLOCKS = capability;
    }

    LinkedHashMap<BlockPos, SpreadPath> paths = new LinkedHashMap<>();

    @Override
    public LinkedHashMap<BlockPos, SpreadPath> getMap() {
        return paths;
    }

    @Override
    public void setPaths(LinkedHashMap<BlockPos, SpreadPath> map)
    {
        this.paths = map;
    }

    @Override
    public void set(SpreadPath pos) {
        paths.put(pos.getPos(), pos);
    }

    @Override
    public void addPaths(LinkedHashMap<BlockPos, SpreadPath> map) {
        map.forEach((pos, path) -> {
            if (!paths.containsKey(pos))
            {
                paths.put(pos, path);
            }
        });
    }

    @Override
    public void remove(SpreadPath pos) {
        paths.remove(pos);
    }

    @Override
    public void clear() {
        paths.clear();
    }
}
