package dev.momostudios.coldsweat.api.event.common;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

public class BlockStateChangedEvent extends Event
{
    private final BlockPos pos;
    private final World world;
    private final BlockState oldState, newState;

    public BlockStateChangedEvent(BlockPos pos, World world, BlockState oldState, BlockState newState)
    {
        this.pos = pos;
        this.world = world;
        this.oldState = oldState;
        this.newState = newState;
    }

    public BlockPos getPosition()
    {   return pos;
    }

    public World getWorld()
    {   return world;
    }

    public BlockState getOldState()
    {   return oldState;
    }

    public BlockState getNewState()
    {   return newState;
    }
}
