package dev.momostudios.coldsweat.common.temperature.modifier.block;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import dev.momostudios.coldsweat.util.math.CSMath;

public class IceBlockEffect extends BlockEffect
{
    public IceBlockEffect()
    {
        super(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE);
    }

    @Override
    public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
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
    public double minEffect() {
        return CSMath.convertUnits(-30, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double minTemperature() {
        return CSMath.convertUnits(32, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
