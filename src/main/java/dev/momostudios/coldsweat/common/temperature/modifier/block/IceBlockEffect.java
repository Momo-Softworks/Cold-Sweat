package dev.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.Units;

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
            return -CSMath.blend(temp, 0, distance, 0.5, 1.5);
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
    public double minEffect() {
        return CSMath.convertUnits(-30, Units.F, Units.MC, false);
    }

    @Override
    public double minTemperature() {
        return CSMath.convertUnits(32, Units.F, Units.MC, true);
    }
}
