package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class NetherPortalBlockEffect extends BlockEffect
{

    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        boolean isInOverworld = player.world.getDimensionKey().getLocation().getPath().equals("overworld");
        return isInOverworld ? 0.3 / Math.max(1, distance) : 0;
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() == net.minecraft.block.Blocks.NETHER_PORTAL;
    }
}
