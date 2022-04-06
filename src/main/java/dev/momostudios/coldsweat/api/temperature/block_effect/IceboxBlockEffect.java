package dev.momostudios.coldsweat.api.temperature.block_effect;

import dev.momostudios.coldsweat.common.block.IceboxBlock;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.core.init.BlockInit;
import dev.momostudios.coldsweat.util.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import dev.momostudios.coldsweat.util.math.CSMath;

public class IceboxBlockEffect extends BlockEffect
{
    public IceboxBlockEffect()
    {
        super(ModBlocks.ICEBOX);
    }

    @Override
    public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
    {
        if (this.hasBlock(state.getBlock()) && state.getValue(IceboxBlock.FROSTED))
        {
            return CSMath.blend(-0.27, 0, distance, 0.5, 5);
        }
        return 0;
    }

    @Override
    public double minEffect() {
        return CSMath.convertUnits(-40, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double minTemperature() {
        return CSMath.convertUnits(32, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
