package com.momosoftworks.coldsweat.common.entity.task;

import com.google.common.collect.ImmutableMap;
import com.momosoftworks.coldsweat.core.init.MemoryInit;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;
import java.util.function.Function;

public class TemptTask extends Task<CreatureEntity>
{
    private final Function<LivingEntity, Float> speed;

    public TemptTask(Function<LivingEntity, Float> speed)
    {
        super(Util.make(() -> {
            ImmutableMap.Builder<MemoryModuleType<?>, MemoryModuleStatus> builder = ImmutableMap.builder();
            builder.put(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED);
            builder.put(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED);
            builder.put(MemoryInit.TEMPTATION_COOLDOWN_TICKS.get(), MemoryModuleStatus.VALUE_ABSENT);
            builder.put(MemoryInit.IS_TEMPTED.get(), MemoryModuleStatus.REGISTERED);
            builder.put(MemoryInit.TEMPTING_PLAYER.get(), MemoryModuleStatus.VALUE_PRESENT);
            builder.put(MemoryModuleType.BREED_TARGET, MemoryModuleStatus.VALUE_ABSENT);
            return builder.build();
        }));
        this.speed = speed;
    }

    protected float getSpeed(CreatureEntity entity)
    {   return this.speed.apply(entity);
    }

    private Optional<PlayerEntity> getTemptingPlayer(CreatureEntity entity)
    {   return entity.getBrain().getMemory((MemoryModuleType) MemoryInit.TEMPTING_PLAYER.get());
    }

    protected boolean timedOut(long gameTime)
    {   return false;
    }

    protected boolean canStillUse(ServerWorld worldIn, CreatureEntity entityIn, long gameTimeIn)
    {   return this.getTemptingPlayer(entityIn).isPresent() && !entityIn.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    protected void start(ServerWorld worldIn, CreatureEntity entityIn, long gameTimeIn)
    {   entityIn.getBrain().setMemory((MemoryModuleType) MemoryInit.IS_TEMPTED.get(), true);
    }

    protected void stop(ServerWorld worldIn, CreatureEntity entityIn, long gameTimeIn)
    {
        Brain<?> brain = entityIn.getBrain();
        brain.setMemory((MemoryModuleType) MemoryInit.TEMPTATION_COOLDOWN_TICKS.get(), 100);
        brain.setMemory((MemoryModuleType) MemoryInit.IS_TEMPTED.get(), false);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    protected void tick(ServerWorld worldIn, CreatureEntity owner, long gameTime)
    {
        PlayerEntity player = (PlayerEntity) this.getTemptingPlayer(owner).get();
        Brain<?> brain = owner.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(player, true));
        if (owner.distanceToSqr(player) < 6.25)
        {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        }
        else
        {   brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityPosWrapper(player, false), this.getSpeed(owner), 2));
        }
    }
}
