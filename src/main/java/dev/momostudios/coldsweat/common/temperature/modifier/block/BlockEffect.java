package dev.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockEffect
{
    /**
     * @param state is the {@link BlockState} of the block
     * @param pos is the position of the block
     * @param distance is the distance between the player and the block
     * @return the temperature of the block. This is ADDED to the world temperature.
     * Temperature is on the Minecraft scale, in which 0 is a snow biome and 2 is a desert (see {@link LavaBlockEffect} for an example)
     */
    public abstract double getTemperature(Player player, BlockState state, BlockPos pos, double distance);

    public abstract boolean hasBlock(BlockState block);

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
     * The maximum world temperature for this BlockEffect to be effective<br>
     * @return a double representing the temperature, in Minecraft units
     */
    public double maxTemperature() {
        return Double.MAX_VALUE;
    }

    /**
     * The minimum world temperature for this BlockEffect to be effective<br>
     * @return a double representing the temperature, in Minecraft units
     */
    public double minTemperature() {
        return -Double.MAX_VALUE;
    }
}
