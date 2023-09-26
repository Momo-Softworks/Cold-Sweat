package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.block.BoilerBlock;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BoilerBlockTemp extends BlockTemp
{
    public BoilerBlockTemp()
    {
        super(ModBlocks.BOILER);
    }

    @Override
    public double getTemperature(World world, LivingEntity entity, BlockState state, BlockPos pos, double distance)
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
