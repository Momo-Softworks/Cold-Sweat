package com.momosoftworks.coldsweat.api.event.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

public class BlockStateChangedEvent extends Event
{
    private final BlockPos pos;
    private final Level level;
    private final BlockState oldState, newState;

    public BlockStateChangedEvent(BlockPos pos, Level level, BlockState oldState, BlockState newState)
    {
        this.pos = pos;
        this.level = level;
        this.oldState = oldState;
        this.newState = newState;
    }

    public BlockPos getPosition()
    {   return pos;
    }

    public Level getLevel()
    {   return level;
    }

    public BlockState getOldState()
    {   return oldState;
    }

    public BlockState getNewState()
    {   return newState;
    }
}
