package com.momosoftworks.coldsweat.api.temperature.block_temp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public class NetherPortalBlockTemp extends BlockTemp
{
    public NetherPortalBlockTemp()
    {   super(Blocks.NETHER_PORTAL);
    }

    @Override
    public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        boolean isInOverworld = level.dimension().location().equals(DimensionType.OVERWORLD_LOCATION.location());
        return isInOverworld ? 0.3 : -0.2;
    }

    @Override
    public double getMaxEffect(@Nullable LivingEntity entity, Level level, BlockPos pos, BlockState state)
    {   return 1;
    }

    @Override
    public double getMinEffect(@Nullable LivingEntity entity, Level level, BlockPos pos, BlockState state)
    {   return -1;
    }

    @Override
    public double getMaxTemp(@Nullable LivingEntity entity, Level level, BlockPos pos, BlockState state)
    {   return level.dimension().location().equals(DimensionType.OVERWORLD_LOCATION.location()) ? Double.POSITIVE_INFINITY : 0;
    }

    @Override
    public double getMinTemp(@Nullable LivingEntity entity, Level level, BlockPos pos, BlockState state)
    {   return level.dimension().location().equals(DimensionType.OVERWORLD_LOCATION.location()) ? Double.POSITIVE_INFINITY : 1;
    }
}
