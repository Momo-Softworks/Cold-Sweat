package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HearthRadiusCapability implements IBlockStorageCap
{
    public static Capability<IBlockStorageCap> HEARTH_BLOCKS;

    @CapabilityInject(IBlockStorageCap.class)
    private static void onCapInit(Capability<IBlockStorageCap> capability)
    {
        HEARTH_BLOCKS = capability;
    }

    HashSet<BlockPos> points = new HashSet<>();

    @Override
    public HashSet<BlockPos> getHashSet() {
        return points;
    }

    @Override
    public void setHashSet(HashSet<BlockPos> list)
    {
        this.points.clear();
        this.points.addAll(list);
    }

    @Override
    public void add(BlockPos pos) {
        points.add(pos);
    }

    @Override
    public void addAll (List<BlockPos> posList) {
        points.addAll(posList);
    }

    @Override
    public void remove(BlockPos pos) {
        points.remove(pos);
    }

    @Override
    public void clear() {
        points.clear();
    }
}
