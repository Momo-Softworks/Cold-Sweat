package dev.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.Units;
import net.minecraft.world.level.material.FlowingFluid;

public class LavaBlockEffect extends BlockEffect
{
    @Override
    public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
    {
        if (hasBlock(state))
        {
            double temp = 0.05 + (15 - state.getValue(FlowingFluid.LEVEL)) / 80d;
            return CSMath.blend(temp, 0, distance, 0.5, 7);
        }
        return 0;
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() == Blocks.LAVA;
    }

    @Override
    public double maxEffect() {
        return CSMath.convertUnits(1000, Units.F, Units.MC, false);
    }

    @Override
    public double maxTemperature() {
        return CSMath.convertUnits(1000, Units.F, Units.MC, true);
    }
}
