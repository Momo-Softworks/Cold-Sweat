package dev.momostudios.coldsweat.api.temperature.block_temp;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.core.BlockPos;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceBlockTemp extends BlockTemp
{
    @Override
    public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
    {
        if (this.hasBlock(state.getBlock()) && state.getValue(AbstractFurnaceBlock.LIT))
        {
            return CSMath.blend(0.32, 0, distance, 0.5, 7);
        }
        return 0;
    }

    @Override
    public boolean hasBlock(Block block)
    {
        super.hasBlock(block);
        return block instanceof AbstractFurnaceBlock;
    }

    @Override
    public double maxEffect() {
        return CSMath.convertUnits(40, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double maxTemperature() {
        return CSMath.convertUnits(600, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
