package com.momosoftworks.coldsweat.api.temperature.block_temp;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A BlockTemp defined entirely by static values, except for its {@link BlockTemp#getTemperature(World, LivingEntity, BlockState, BlockPos, double) getTemperature()} method.
 */
public abstract class SimpleBlockTemp extends BlockTemp
{
    protected final double minEffect;
    protected final double maxEffect;
    protected final double minTemp;
    protected final double maxTemp;
    protected final double range;
    protected final boolean fade;
    protected final boolean logarithmic;

    public SimpleBlockTemp(double minEffect, double maxEffect, double minTemp, double maxTemp, double range, boolean fade, boolean logarithmic, Block... blocks)
    {
        super(blocks);
        this.minEffect = minEffect;
        this.maxEffect = maxEffect;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.range = range;
        this.fade = fade;
        this.logarithmic = logarithmic;
    }

    @Override
    public double getMinEffect(LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return this.minEffect;
    }

    @Override
    public double getMaxEffect(LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return this.maxEffect;
    }

    @Override
    public double getMinTemp(LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return this.minTemp;
    }

    @Override
    public double getMaxTemp(LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return this.maxTemp;
    }

    @Override
    public double getRange(LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return this.range;
    }

    @Override
    public boolean fades(LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return this.fade;
    }

    @Override
    public boolean isLogarithmic(LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return this.logarithmic;
    }
}
