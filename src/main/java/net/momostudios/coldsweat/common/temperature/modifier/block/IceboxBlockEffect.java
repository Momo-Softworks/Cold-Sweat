package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.common.block.IceboxBlock;
import net.momostudios.coldsweat.util.MathHelperCS;
import net.momostudios.coldsweat.util.Units;

public class IceboxBlockEffect extends BlockEffect
{
    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        if (this.hasBlock(state) && state.get(IceboxBlock.FROSTED))
        {
            return MathHelperCS.blend(-0.27, 0, distance, 0.5, 5);
        }
        return 0;
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() instanceof IceboxBlock;
    }

    @Override
    public double minTemp() {
        return MathHelperCS.convertUnits(-40, Units.F, Units.MC, false);
    }
}
