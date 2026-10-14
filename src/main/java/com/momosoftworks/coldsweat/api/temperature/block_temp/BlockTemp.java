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
    @Nullable
    private final LegacyProperties legacyProperties;

    public abstract double getTemperature(World world, @Nullable LivingEntity entity, BlockState state, BlockPos pos, double distance);

    public boolean isValid(World level, BlockPos pos, BlockState state)
    {   return true;
    }

    public BlockTemp(Block... blocks)
    {
        this.validBlocks = ImmutableSet.<Block>builder().add(blocks).build();
        this.legacyProperties = null;
    }

    /**
     * DEPRECATED: Override the dynamic property getters (i.e. {@link #getMaxEffect(LivingEntity, World, BlockPos, BlockState)}) instead
     */
    @Deprecated
    public BlockTemp(double minEffect, double maxEffect, double minTemp, double maxTemp, double range, boolean fade, boolean logarithmic, Block... blocks)
    {
        this.validBlocks = ImmutableSet.<Block>builder().add(blocks).build();
        this.legacyProperties = new LegacyProperties(minEffect, maxEffect, minTemp, maxTemp, range, fade, logarithmic);
    }

    /**
     * DEPRECATED: Override the dynamic property getters (i.e. {@link #getMaxEffect(LivingEntity, World, BlockPos, BlockState)}) instead
     */
    @Deprecated
    public BlockTemp(double minEffect, double maxEffect, double minTemp, double maxTemp, double range, boolean fade, Block... blocks)
    {   this(minEffect, maxEffect, minTemp, maxTemp, range, fade, false, blocks);
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
    {   return legacyProperties != null ? legacyProperties.maxEffect() : Double.POSITIVE_INFINITY;
    }

    /**
     * DEPRECATED: Use {@link #getMinEffect(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public double minEffect()
    {   return legacyProperties != null ? legacyProperties.minEffect() : Double.NEGATIVE_INFINITY;
    }

    /**
     * DEPRECATED: Use {@link #getMaxTemp(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public double maxTemperature()
    {   return legacyProperties != null ? legacyProperties.maxTemperature() : Double.POSITIVE_INFINITY;
    }

    /**
     * DEPRECATED: Use {@link #getMinTemp(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public double minTemperature()
    {   return legacyProperties != null ? legacyProperties.minTemperature() : Double.NEGATIVE_INFINITY;
    }

    /**
     * DEPRECATED: Use {@link #isLogarithmic(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public boolean logarithmic()
    {   return legacyProperties != null ? legacyProperties.logarithmic() : false;
    }

    /**
     * DEPRECATED: Use {@link #getRange(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public double range()
    {   return legacyProperties != null ? legacyProperties.range() : ConfigSettings.BLOCK_RANGE.get();
    }

    /**
     * DEPRECATED: Use {@link #fades(LivingEntity, World, BlockPos, BlockState)} instead
     */
    @Deprecated
    public boolean fade()
    {   return legacyProperties != null ? legacyProperties.fade() : true;
    }

    /**
     * Holds the statically-defined properties passed to {@link BlockTemp}'s deprecated constructors.<br>
     * Exists only for backwards compatibility, and will be removed alongside those constructors.
     */
    @Deprecated
    protected static class LegacyProperties
    {
        double minEffect;
        double maxEffect;
        double minTemperature;
        double maxTemperature;
        double range;
        boolean fade;
        boolean logarithmic;

        protected LegacyProperties(double minEffect, double maxEffect, double minTemperature, double maxTemperature, double range, boolean fade, boolean logarithmic)
        {
            this.minEffect = minEffect;
            this.maxEffect = maxEffect;
            this.minTemperature = minTemperature;
            this.maxTemperature = maxTemperature;
            this.range = range;
            this.fade = fade;
            this.logarithmic = logarithmic;
        }

        protected double minEffect() { return minEffect; }
        protected double maxEffect() { return maxEffect; }
        protected double minTemperature() { return minTemperature; }
        protected double maxTemperature() { return maxTemperature; }
        protected double range() { return range; }
        protected boolean fade() { return fade; }
        protected boolean logarithmic() { return logarithmic; }
    }
}
