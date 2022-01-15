package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.momostudios.coldsweat.util.SpreadPath;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class HearthRadiusCapability implements IBlockStorageCap
{
    public static Capability<IBlockStorageCap> HEARTH_BLOCKS;

    @CapabilityInject(IBlockStorageCap.class)
    private static void onCapInit(Capability<IBlockStorageCap> capability)
    {
        HEARTH_BLOCKS = capability;
    }

    HashSet<SpreadPath> paths = new HashSet<>();
    HashSet<BlockPos> points = new HashSet<>();

    @Override
    public HashSet<SpreadPath> getHashSet() {
        return paths;
    }

    @Override
    public HashSet<BlockPos> getPositions() {
        return points;
    }

    @Override
    public void setPaths(HashSet<SpreadPath> list)
    {
        this.paths = list;
        this.points = paths.stream().map(SpreadPath::getPos).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public void add(SpreadPath pos) {
        paths.add(pos);
    }

    @Override
    public void addPaths(Collection<SpreadPath> posList) {
        paths.addAll(posList);
    }

    @Override
    public void addPoints(Collection<BlockPos> posList) {
        points.addAll(posList);
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
