package com.momosoftworks.coldsweat.util.render;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class ContainerOpenersCounter
{
    private static final int CHECK_TICK_DELAY = 5;
    private int openCount;

    protected abstract void onOpen(World level, BlockPos pos, BlockState state);

    protected abstract void onClose(World level, BlockPos pos, BlockState state);

    protected abstract void openerCountChanged(World level, BlockPos pos, BlockState state, int eventId, int eventParam);

    protected abstract boolean isOwnContainer(PlayerEntity player);

    public void incrementOpeners(PlayerEntity player, World level, BlockPos pos, BlockState state)
    {
        int i = this.openCount++;
        if (i == 0)
        {   this.onOpen(level, pos, state);
            level.blockEvent(pos, state.getBlock(), 1, this.openCount);
            scheduleRecheck(level, pos, state);
        }

        this.openerCountChanged(level, pos, state, i, this.openCount);
    }

    public void decrementOpeners(PlayerEntity player, World level, BlockPos pos, BlockState state)
    {
        int i = this.openCount--;
        if (this.openCount == 0)
        {   this.onClose(level, pos, state);
            level.blockEvent(pos, state.getBlock(), 1, this.openCount);
        }

        this.openerCountChanged(level, pos, state, i, this.openCount);
    }

    private int getOpenCount(World level, BlockPos pos)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        float f = 5.0F;
        AxisAlignedBB aabb = new AxisAlignedBB((double)((float)i - 5.0F), (double)((float)j - 5.0F), (double)((float)k - 5.0F), (double)((float)(i + 1) + 5.0F), (double)((float)(j + 1) + 5.0F), (double)((float)(k + 1) + 5.0F));
        return level.getEntities(EntityType.PLAYER, aabb, this::isOwnContainer).size();
    }

    public void recheckOpeners(World level, BlockPos pos, BlockState state)
    {
        int i = this.getOpenCount(level, pos);
        int j = this.openCount;
        if (j != i)
        {
            boolean flag = i != 0;
            boolean flag1 = j != 0;
            if (flag && !flag1)
            {   this.onOpen(level, pos, state);
                level.blockEvent(pos, state.getBlock(), 1, this.openCount);
            }
            else if (!flag)
            {   this.onClose(level, pos, state);
                level.blockEvent(pos, state.getBlock(), 1, this.openCount);
            }

            this.openCount = i;
        }

        this.openerCountChanged(level, pos, state, j, i);
        if (i > 0)
        {   scheduleRecheck(level, pos, state);
        }
    }

    public int getOpenerCount()
    {   return this.openCount;
    }

    private static void scheduleRecheck(World level, BlockPos pos, BlockState state)
    {
        if (level instanceof ServerWorld)
        {   state.getBlock().tick(state, ((ServerWorld) level), pos, level.random);
        }
    }
}
