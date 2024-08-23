package com.momosoftworks.coldsweat.common.entity.task;

import com.google.common.collect.ImmutableMap;
import com.momosoftworks.coldsweat.core.init.MemoryInit;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.server.ServerWorld;

public class LeapingChargeTask extends Task<MobEntity>
{
    public final RangedInteger cooldownRange;
    private final SoundEvent sound;

    public LeapingChargeTask(RangedInteger cooldownRange, SoundEvent sound)
    {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryInit.LONG_JUMP_MID_JUMP.get(), MemoryModuleStatus.VALUE_PRESENT), 100);
        this.cooldownRange = cooldownRange;
        this.sound = sound;
    }

    protected boolean canStillUse(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn)
    {   return !entityIn.isOnGround();
    }

    protected void start(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn)
    {   entityIn.setPose(Pose.SPIN_ATTACK);
    }

    protected void stop(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn)
    {
        if (entityIn.isOnGround())
        {
            entityIn.setDeltaMovement(entityIn.getDeltaMovement().scale(0.10000000149011612));
            worldIn.playSound( null, entityIn, this.sound, SoundCategory.NEUTRAL, 2.0F, 1.0F);
        }

        entityIn.setPose(Pose.STANDING);
        entityIn.getBrain().eraseMemory(MemoryInit.LONG_JUMP_MID_JUMP.get());
        entityIn.getBrain().setMemory(MemoryInit.LONG_JUMP_COOLING_DOWN.get(), this.cooldownRange.randomValue(worldIn.getRandom()));
    }
}
