package dev.momostudios.coldsweat.core.capabilities;

import dev.momostudios.coldsweat.util.SpreadPath;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public class HearthRadiusCapability implements IBlockStorageCap
{
    public static Capability<IBlockStorageCap> HEARTH_BLOCKS;

    @CapabilityInject(IBlockStorageCap.class)
    private static void onCapInit(Capability<IBlockStorageCap> capability)
    {
        HEARTH_BLOCKS = capability;
    }

    ConcurrentHashMap<BlockPos, SpreadPath> paths = new ConcurrentHashMap<>();

    @Nonnull
    @Override
    public ConcurrentHashMap<BlockPos, SpreadPath> getMap()
    {
        return paths;
    }

    @Override
    public void setPaths(ConcurrentHashMap<BlockPos, SpreadPath> map)
    {
        this.paths = map;
    }

    @Override
    public void set(SpreadPath pos) {
        paths.put(pos.getPos(), pos);
    }

    @Override
    public void addPaths(ConcurrentHashMap<BlockPos, SpreadPath> map) {
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
