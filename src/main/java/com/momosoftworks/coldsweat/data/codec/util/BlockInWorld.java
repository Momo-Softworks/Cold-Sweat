package com.momosoftworks.coldsweat.data.codec.util;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class BlockInWorld
{
    private final IWorldReader level;
    private final BlockPos pos;
    private final boolean loadChunks;
    @Nullable
    private BlockState state;
    @Nullable
    private TileEntity entity;
    private boolean cachedEntity;

    public BlockInWorld(IWorldReader pLevel, BlockPos pPos, boolean pLoadChunks)
    {
        this.level = pLevel;
        this.pos = pPos.immutable();
        this.loadChunks = pLoadChunks;
    }

    /**
     * Gets the block state as currently held, or (if it has not gotten it from the level) loads it from the level.
     * This will only look up the state from the world if {@link #loadChunks} is true or the block position is loaded.
     */
    public BlockState getState()
    {
        if (this.state == null && (this.loadChunks || this.level.hasChunkAt(this.pos)))
        {   this.state = this.level.getBlockState(this.pos);
        }

        return this.state;
    }

    /**
     * Gets the BlockEntity as currently held, or (if it has not gotten it from the level) loads it from the level.
     */
    @Nullable
    public TileEntity getEntity()
    {
        if (this.entity == null && !this.cachedEntity)
        {   this.entity = this.level.getBlockEntity(this.pos);
            this.cachedEntity = true;
        }
        return this.entity;
    }

    public IWorldReader getLevel()
    {   return this.level;
    }

    public BlockPos getPos()
    {   return this.pos;
    }

    public static Predicate<BlockInWorld> hasState(Predicate<BlockState> pState)
    {
        return (block) -> block != null && pState.test(block.getState());
    }
}
