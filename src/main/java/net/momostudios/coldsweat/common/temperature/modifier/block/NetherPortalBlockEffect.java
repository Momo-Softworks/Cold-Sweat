package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.momostudios.coldsweat.core.util.MathHelperCS;

public class NetherPortalBlockEffect extends BlockEffect
{

    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        boolean isInOverworld = player.world.getDimensionKey().getLocation().toString().equals(DimensionType.OVERWORLD.getLocation().toString());
        return MathHelperCS.blend(isInOverworld ? 0.3 : -0.2, 0, distance, 0, 7);
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() == net.minecraft.block.Blocks.NETHER_PORTAL;
    }

    @Override
    public double maxTemp()
    {
        return 1.8;
    }
    @Override
    public double minTemp()
    {
        return -1.2;
    }
}
