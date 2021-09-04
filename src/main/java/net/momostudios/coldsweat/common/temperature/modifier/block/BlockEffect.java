package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public abstract class BlockEffect
{
    /**
     * @param state is the {@link BlockState} of the block
     * @param pos is the position of the block
     * @param distance is the distance between the player and the block
     * @return the temperature of the block. This is ADDED to the ambient temperature.
     * Temperature is on the Minecraft scale, in which 0 is a snow biome and 2 is a desert (see {@link LavaBlockEffect} for an example)
     */
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        return 0;
    }
    
    public boolean hasBlock(BlockState block)
    {
        return false;
    }
}
