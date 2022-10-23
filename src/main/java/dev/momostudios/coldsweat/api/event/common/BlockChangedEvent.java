package dev.momostudios.coldsweat.api.event.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

public class BlockChangedEvent extends Event
{
    BlockPos pos;
    BlockState prevState;
    BlockState newState;
    Level level;

    public BlockChangedEvent(BlockPos pos, BlockState prevState, BlockState newState, Level level)
    {
        this.pos = pos;
        this.prevState = prevState;
        this.newState = newState;
        this.level = level;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public BlockState getPrevState()
    {
        return prevState;
    }

    public BlockState getNewState()
    {
        return newState;
    }

    public Level getLevel()
    {
        return level;
    }
}
