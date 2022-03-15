package dev.momostudios.coldsweat.common.temperature.modifier.block;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.core.BlockPos;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FireBlockEffect extends BlockEffect
{
    public FireBlockEffect()
    {
        super(Blocks.FIRE);
    }

    @Override
    public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
    {
        if (hasBlock(state))
        {
            return CSMath.blend(0.2, 0, distance, 0.5, 7);
        }
        return 0;
    }

    @Override
    public double maxEffect() {
        return CSMath.convertUnits(32, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double maxTemperature() {
        return CSMath.convertUnits(400, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
