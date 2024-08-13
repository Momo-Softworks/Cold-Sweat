package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

public class NetherPortalBlockTemp extends BlockTemp
{
    public NetherPortalBlockTemp()
    {
        super(Blocks.NETHER_PORTAL);
    }

    @Override
    public double getTemperature(World world, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        boolean isInOverworld = world.dimension().location().equals(DimensionType.OVERWORLD_LOCATION.location());
        return CSMath.blend(isInOverworld ? 0.3 : -0.2, 0, distance, 0, 3);
    }

    @Override
    public double maxEffect()
    {   return 1;
    }

    @Override
    public double minEffect()
    {   return -1;
    }

    @Override
    public double minTemperature()
    {   return 1;
    }
}
