package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.core.util.MathHelperCS;

public class IceBlockEffect extends BlockEffect
{
    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        if (hasBlock(state))
        {
            double temp =
                    state.getBlock() == Blocks.ICE ? -0.02 :
                    state.getBlock() == Blocks.PACKED_ICE ? -0.06 :
                    state.getBlock() == Blocks.BLUE_ICE ? -0.18 : 0;
            return Math.min(0, temp * Math.max(0, 3 - distance));
        }
        return 0;
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() == Blocks.ICE ||
               block.getBlock() == Blocks.PACKED_ICE ||
               block.getBlock() == Blocks.BLUE_ICE;
    }

    @Override
    public double minTemp() {
        return MathHelperCS.convertFromF(-100);
    }
}
