package dev.momostudios.coldsweat.api.temperature.block_temp;

import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.core.BlockPos;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CampfireBlockTemp extends BlockTemp
{
    public CampfireBlockTemp()
    {
        super(Blocks.CAMPFIRE);
    }

    @Override
    public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
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
