package dev.momostudios.coldsweat.common.temperature.modifier.block;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.world.level.material.FluidState;

public class LavaBlockEffect extends BlockEffect
{
    @Override
    public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
    {
        if (hasBlock(state))
        {
            FluidState fluidState = state.getFluidState();
            double temp = 0.05 + (fluidState.getOwnHeight()) / 8d;
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
        return CSMath.convertUnits(1000, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double maxTemperature() {
        return CSMath.convertUnits(1000, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
