package dev.momosoftworks.coldsweat.api.temperature.block_temp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public abstract class BlockTemp
{
    private final HashSet<Block> validBlocks;

    /**
     * @param state is the {@link BlockState} of the block
     * @param pos is the position of the block
     * @param distance is the distance between the player and the block
     * @return the temperature of the block. This is ADDED to the world temperature.
     * Temperature is on the Minecraft scale, in which 0 is a snow biome and 2 is a desert (see {@link LavaBlockTemp} for an example)
     */
    public abstract double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance);

    public BlockTemp(Block... blocks)
    {   validBlocks = new HashSet<>(Set.of(blocks));
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
