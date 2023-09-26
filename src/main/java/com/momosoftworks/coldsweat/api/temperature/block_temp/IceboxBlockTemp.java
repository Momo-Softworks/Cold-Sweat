package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.block.IceboxBlock;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IceboxBlockTemp extends BlockTemp
{
    public IceboxBlockTemp()
    {
        super(ModBlocks.ICEBOX);
    }

    @Override
    public double getTemperature(World world, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        if (state.getValue(IceboxBlock.FROSTED))
        {   return CSMath.blend(-0.27, 0, distance, 0.5, 5);
        }
        return 0;
    }

    @Override
    public double minEffect() {
        return Temperature.convertUnits(-40, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double minTemperature() {
        return Temperature.convertUnits(32, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
