package com.momosoftworks.coldsweat.api.temperature.block_temp;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class NetherPortalBlockTemp extends BlockTemp
{
    public NetherPortalBlockTemp()
    {   super(Blocks.NETHER_PORTAL);
    }

    @Override
    public double getTemperature(World world, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        boolean isInOverworld = world.dimension().location().equals(DimensionType.OVERWORLD_LOCATION.location());
        return isInOverworld ? 0.3 : -0.2;
    }

    @Override
    public double getMaxEffect(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return 1;
    }

    @Override
    public double getMinEffect(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return -1;
    }

    @Override
    public double getMaxTemp(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return level.dimension().location().equals(DimensionType.OVERWORLD_LOCATION.location()) ? Double.POSITIVE_INFINITY : 0;
    }

    @Override
    public double getMinTemp(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return level.dimension().location().equals(DimensionType.OVERWORLD_LOCATION.location()) ? Double.POSITIVE_INFINITY : 1;
    }
}
