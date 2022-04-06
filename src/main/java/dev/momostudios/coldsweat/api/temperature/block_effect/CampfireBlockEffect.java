package dev.momostudios.coldsweat.api.temperature.block_effect;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.core.BlockPos;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CampfireBlockEffect extends BlockEffect
{
    public CampfireBlockEffect()
    {
        super(Blocks.CAMPFIRE);
    }

    @Override
    public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
    {
        if (this.hasBlock(state.getBlock()) && state.getValue(CampfireBlock.LIT))
        {
            return CSMath.blend(0.27, 0, distance, 0.5, 7);
        }
        return 0;
    }

    @Override
    public double maxEffect() {
        return CSMath.convertUnits(40, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double maxTemperature() {
        return CSMath.convertUnits(400, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
