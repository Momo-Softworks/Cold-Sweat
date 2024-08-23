package com.momosoftworks.coldsweat.common.entity.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class WalkTask extends Task<CreatureEntity>
{
    private final float speed;

    public WalkTask(float speed)
    {
        super(ImmutableMap.of(MemoryModuleType.HURT_BY, MemoryModuleStatus.VALUE_PRESENT), 100, 120);
        this.speed = speed;
    }

    protected boolean canStillUse(ServerWorld worldIn, CreatureEntity entityIn, long gameTimeIn)
    {   return true;
    }

    protected void start(ServerWorld worldIn, CreatureEntity entityIn, long gameTimeIn)
    {   entityIn.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    protected void tick(ServerWorld worldIn, CreatureEntity owner, long gameTime)
    {
        if (owner.getNavigation().isDone())
        {
            Vector3d vector3d = RandomPositionGenerator.getLandPos(owner, 5, 4);
            if (vector3d != null)
            {   owner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vector3d, this.speed, 0));
            }
        }
    }
}
