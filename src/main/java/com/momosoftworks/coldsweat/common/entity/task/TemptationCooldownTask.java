package com.momosoftworks.coldsweat.common.entity.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class TemptationCooldownTask extends Task<LivingEntity>
{
    private final MemoryModuleType<Integer> memoryModuleType;

    public TemptationCooldownTask(MemoryModuleType<Integer> memoryModuleType)
    {
        super(ImmutableMap.of(memoryModuleType, MemoryModuleStatus.VALUE_PRESENT));
        this.memoryModuleType = memoryModuleType;
    }

    private Optional<Integer> getTemptationCooldownTicks(LivingEntity entity)
    {   return entity.getBrain().getMemory(this.memoryModuleType);
    }

    protected boolean timedOut(long gameTime)
    {   return false;
    }

    protected boolean canStillUse(ServerWorld worldIn, LivingEntity entityIn, long gameTimeIn)
    {
        Optional<Integer> cooldownTicks = this.getTemptationCooldownTicks(entityIn);
        return cooldownTicks.isPresent() && (Integer) cooldownTicks.get() > 0;
    }

    protected void tick(ServerWorld worldIn, LivingEntity owner, long gameTime)
    {
        Optional<Integer> cooldownTicks = this.getTemptationCooldownTicks(owner);
        owner.getBrain().setMemory(this.memoryModuleType, (Integer) cooldownTicks.get() - 1);
    }

    protected void stop(ServerWorld worldIn, LivingEntity entityIn, long gameTimeIn)
    {   entityIn.getBrain().eraseMemory(this.memoryModuleType);
    }
}
