package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.util.CSMath;
import net.momostudios.coldsweat.util.Units;

public class FurnaceBlockEffect extends BlockEffect
{
    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        if (this.hasBlock(state) && state.get(AbstractFurnaceBlock.LIT))
        {
            return CSMath.blend(0.32, 0, distance, 0.5, 7);
        }
        return 0;
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() instanceof AbstractFurnaceBlock;
    }

    @Override
    public double maxTemp() {
        return CSMath.convertUnits(40, Units.F, Units.MC, false);
    }
}
