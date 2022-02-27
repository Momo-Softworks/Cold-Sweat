package dev.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.Units;

public class MagmaBlockEffect extends BlockEffect
{

    @Override
    public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
    {
        return CSMath.blend(0.2, 0, distance, 0.5, 3);
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() == Blocks.MAGMA_BLOCK;
    }

    @Override
    public double maxEffect()
    {
        return CSMath.convertUnits(30, Units.F, Units.MC, false);
    }

    @Override
    public double maxTemperature() {
        return CSMath.convertUnits(800, Units.F, Units.MC, true);
    }
}
