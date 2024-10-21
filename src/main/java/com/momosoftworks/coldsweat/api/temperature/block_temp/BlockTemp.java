package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public abstract class BlockTemp
{
    private final Set<Block> validBlocks;

    public abstract double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance);

    public boolean isValid(Level level, BlockPos pos, BlockState state)
    {   return true;
    }

    public BlockTemp(Block... blocks)
    {   validBlocks = ImmutableSet.<Block>builder().add(blocks).build();
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
    public double maxEffect() {
        return Double.MAX_VALUE;
    }

    /**
     * The minimum temperature this block can emit, no matter how many there are near the player <br>
     * (Useful for blocks with negative temperature) <br>
     * @return a double representing the temperature, in Minecraft units
     */
    public double minEffect() {
        return -Double.MAX_VALUE;
    }

    /**
     * The maximum world temperature for this BlockTemp to be effective<br>
     * @return a double representing the temperature, in Minecraft units
     */
    public double maxTemperature() {
        return Double.MAX_VALUE;
    }

    /**
     * The minimum world temperature for this BlockTemp to be effective<br>
     * @return a double representing the temperature, in Minecraft units
     */
    public double minTemperature() {
        return -Double.MAX_VALUE;
    }
}
