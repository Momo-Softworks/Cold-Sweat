package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class IceBlockTemp extends BlockTemp
{
    public IceBlockTemp()
    {   super(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE);
    }

    @Override
    public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        Block block = state.getBlock();
        double temperature = block == Blocks.ICE ? -0.15
                           : block == Blocks.PACKED_ICE ? -0.25
                           : block == Blocks.BLUE_ICE ? -0.35 : 0;
        double range = 4;
        return CSMath.blend(0, temperature, distance, range, 0);
    }

    @Override
    public double minTemperature()
    {   return -0.7;
    }
}
