package dev.momosoftworks.coldsweat.api.temperature.block_temp;

import dev.momosoftworks.coldsweat.api.util.Temperature;
import dev.momosoftworks.coldsweat.common.block.BoilerBlock;
import dev.momosoftworks.coldsweat.util.math.CSMath;
import dev.momosoftworks.coldsweat.util.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BoilerBlockTemp extends BlockTemp
{
    public BoilerBlockTemp()
    {
        super(ModBlocks.BOILER);
    }

    @Override
    public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        if (state.getValue(BoilerBlock.LIT))
        {   return CSMath.blend(0.27, 0, distance, 0.5, 7);
        }
        return 0;
    }

    @Override
    public double maxEffect() {
        return Temperature.convertUnits(40, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double maxTemperature() {
        return Temperature.convertUnits(212, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
