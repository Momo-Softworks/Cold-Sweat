package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.ArrayList;
import java.util.List;

public class HearthRadiusCapability implements IBlockStorageCap
{
    public static Capability<IBlockStorageCap> HEARTH_BLOCKS;

    @CapabilityInject(IBlockStorageCap.class)
    private static void onCapInit(Capability<IBlockStorageCap> capability)
    {
        HEARTH_BLOCKS = capability;
    }

    List<BlockPos> points = new ArrayList<>();

    @Override
    public List<BlockPos> getList() {
        return points;
    }

    @Override
    public void setList(List<BlockPos> list) {
        this.points = list;
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
