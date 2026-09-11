package com.momosoftworks.coldsweat.api.temperature.block_temp;


import com.google.common.collect.ImmutableSet;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Set;

public abstract class BlockTemp
{
    private final Set<Block> validBlocks;

    public abstract double getTemperature(World world, @Nullable LivingEntity entity, BlockState state, BlockPos pos, double distance);

    public boolean isValid(World level, BlockPos pos, BlockState state)
    {   return true;
    }

    public BlockTemp(Block... blocks)
    {   this.validBlocks = ImmutableSet.<Block>builder().add(blocks).build();
    }

    public boolean hasBlock(Block block)
    {   return validBlocks.contains(block);
    }

    public Set<Block> getAffectedBlocks()
    {   return validBlocks;
    }

    /*
     Property getters. By default, they delegate to their deprecated counterparts
    */

    /**
     * The maximum <b>increase</b> in world temperature that any quantify of this block can cause to an entity, in MC units
     */
    public double getMaxEffect(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return maxEffect();
    }

    /**
     * The maximum <b>decrease</b> in world temperature that any quantify of this block can cause to an entity, in MC units
     */
    public double getMinEffect(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return minEffect();
    }

    /**
     * The maximum temperature at which this block can be effective, in MC units. <br>
     * This block may not increase the world temperature above the returned value.
     */
    public double getMaxTemp(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return maxTemperature();
    }

    /**
     * The minimum temperature at which this block can be effective, in MC units. <br>
     * This block may not decrease the world temperature below the returned value.
     */
    public double getMinTemp(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return minTemperature();
    }

    /**
     * The range of this block's effect, in blocks.
     */
    public double getRange(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return range();
    }

    /**
     * Whether multiple of this block will affect the entity logarithmically (diminishing returns) or linearly.<br>
     * Defaults to linear scaling.
     */
    public boolean isLogarithmic(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return logarithmic();
    }

    /**
     * Whether the effect of this block fades over distance.
     */
    public boolean fades(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return fade();
    }

    /**
     * DEPRECATED: Use {@link #getMaxEffect(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public double maxEffect()
    {   return Double.POSITIVE_INFINITY;
    }

    /**
     * DEPRECATED: Use {@link #getMinEffect(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public double minEffect()
    {   return Double.NEGATIVE_INFINITY;
    }

    /**
     * DEPRECATED: Use {@link #getMaxTemp(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public double maxTemperature()
    {   return Double.POSITIVE_INFINITY;
    }

    /**
     * DEPRECATED: Use {@link #getMinTemp(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public double minTemperature()
    {   return Double.NEGATIVE_INFINITY;
    }

    /**
     * DEPRECATED: Use {@link #isLogarithmic(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public boolean logarithmic()
    {   return false;
    }

    /**
     * DEPRECATED: Use {@link #getRange(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public double range()
    {   return ConfigSettings.BLOCK_RANGE.get();
    }

    /**
     * DEPRECATED: Use {@link #fades(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public boolean fade()
    {   return true;
    }
}
