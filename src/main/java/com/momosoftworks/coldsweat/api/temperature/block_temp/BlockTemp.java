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
    private final double maxEffect;
    private final double minEffect;
    private final double maxTemperature;
    private final double minTemperature;
    private final double range;
    private final boolean fade;
    private final boolean logarithmic;

    public abstract double getTemperature(World world, @Nullable LivingEntity entity, BlockState state, BlockPos pos, double distance);

    public boolean isValid(World level, BlockPos pos, BlockState state)
    {   return true;
    }

    public BlockTemp(double minEffect, double maxEffect, double minTemp, double maxTemp, double range, boolean fade, boolean logarithmic, Block... blocks)
    {
        this.validBlocks = ImmutableSet.<Block>builder().add(blocks).build();
        this.minEffect = minEffect;
        this.maxEffect = maxEffect;
        this.minTemperature = minTemp;
        this.maxTemperature = maxTemp;
        this.range = range;
        this.fade = fade;
        this.logarithmic = logarithmic;
    }

    public BlockTemp(double minEffect, double maxEffect, double minTemp, double maxTemp, double range, boolean fade, Block... blocks)
    {
        this(minEffect, maxEffect, minTemp, maxTemp, range, fade, false, blocks);
    }

    public BlockTemp(Block... blocks)
    {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
             Double.POSITIVE_INFINITY, true, false, blocks);
    }

    public boolean hasBlock(Block block)
    {   return validBlocks.contains(block);
    }

    public Set<Block> getAffectedBlocks()
    {   return validBlocks;
    }

    /**
     * The maximum temperature this block can emit, no matter how many there are near the player <br>
     * @return a double representing the temperature, in Minecraft units
     */
    public double maxEffect()
    {   return maxEffect;
    }

    /**
     * The minimum temperature this block can emit, no matter how many there are near the player <br>
     * (Useful for blocks with negative temperature) <br>
     * @return a double representing the temperature, in Minecraft units
     */
    public double minEffect()
    {   return minEffect;
    }

    /**
     * The maximum world temperature for this BlockTemp to be effective<br>
     * @return a double representing the temperature, in Minecraft units
     */
    public double maxTemperature()
    {   return maxTemperature;
    }

    /**
     * The minimum world temperature for this BlockTemp to be effective<br>
     * @return a double representing the temperature, in Minecraft units
     */
    public double minTemperature()
    {   return minTemperature;
    }

    /**
     * As more of these blocks are present, the temperature will change with diminishing returns
     */
    public boolean logarithmic()
    {   return logarithmic;
    }

    public double range()
    {
        return Math.min(range, ConfigSettings.BLOCK_RANGE.get());
    }

    public boolean fade()
    {   return fade;
    }
}
