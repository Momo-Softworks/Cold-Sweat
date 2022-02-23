package dev.momostudios.coldsweat.common.temperature.modifier.block;

import dev.momostudios.coldsweat.common.block.IceboxBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.Units;

public class IceboxBlockEffect extends BlockEffect
{
    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        if (this.hasBlock(state) && state.get(IceboxBlock.FROSTED))
        {
            return CSMath.blend(-0.27, 0, distance, 0.5, 5);
        }
        return 0;
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() instanceof IceboxBlock;
    }

    @Override
    public double minEffect() {
        return CSMath.convertUnits(-40, Units.F, Units.MC, false);
    }

    @Override
    public double minTemperature() {
        return CSMath.convertUnits(32, Units.F, Units.MC, true);
    }
}
