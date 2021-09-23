package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;

public class FurnaceBlockEffect extends BlockEffect
{
    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        if (this.hasBlock(state) && state.get(AbstractFurnaceBlock.LIT))
        {
            double temp = 0.04;
            return Math.max(0, temp * (9 - distance));
        }
        return 0;
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() instanceof AbstractFurnaceBlock;
    }
}
