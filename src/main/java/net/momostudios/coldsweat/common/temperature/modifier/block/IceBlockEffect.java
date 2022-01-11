package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.Units;

public class IceBlockEffect extends BlockEffect
{
    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        if (hasBlock(state))
        {
            double temp =
                    state.getBlock() == Blocks.ICE ? 0.2 :
                    state.getBlock() == Blocks.PACKED_ICE ? 0.3 :
                    state.getBlock() == Blocks.BLUE_ICE ? 0.4 : 0;
            return -MathHelperCS.blend(temp, 0, distance, 0.5, 1.5);
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
        return MathHelperCS.convertUnits(-30, Units.F, Units.MC, false);
    }
}
