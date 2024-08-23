package com.momosoftworks.coldsweat.common.entity.task;

import com.google.common.collect.ImmutableMap;
import com.momosoftworks.coldsweat.core.init.MemoryInit;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class RamImpactTask<E extends CreatureEntity> extends Task<E>
{
    private final Function<E, RangedInteger> cooldownRange;
    private final EntityPredicate entityPredicate;
    private final float speed;
    private final ToDoubleFunction<E> strengthMultiplier;
    private Vector3d direction;
    private final Function<E, SoundEvent> sound;

    public RamImpactTask(Function<E, RangedInteger> cooldownRange, EntityPredicate entityPredicate, float speed, ToDoubleFunction<E> strengthMultiplier, Function<E, SoundEvent> sound)
    {
        super(ImmutableMap.of(MemoryInit.RAM_COOLDOWN_TICKS.get(), MemoryModuleStatus.VALUE_ABSENT, MemoryInit.RAM_TARGET.get(), MemoryModuleStatus.VALUE_PRESENT), 200);
        this.cooldownRange = cooldownRange;
        this.entityPredicate = entityPredicate;
        this.speed = speed;
        this.strengthMultiplier = strengthMultiplier;
        this.direction = Vector3d.ZERO;
        this.sound = sound;
    }

    protected boolean checkExtraStartConditions(ServerWorld worldIn, E owner)
    {   return owner.getBrain().hasMemoryValue(MemoryInit.RAM_TARGET.get());
    }

    protected boolean canStillUse(ServerWorld worldIn, E entityIn, long gameTimeIn)
    {   return entityIn.getBrain().hasMemoryValue(MemoryInit.RAM_TARGET.get());
    }

    protected void start(ServerWorld worldIn, E entityIn, long gameTimeIn)
    {
        BlockPos pos = entityIn.blockPosition();
        Brain<?> brain = entityIn.getBrain();
        Vector3d targetPos = (Vector3d) brain.getMemory((MemoryModuleType) MemoryInit.RAM_TARGET.get()).get();
        this.direction = new Vector3d((double) pos.getX() - targetPos.x(), 0.0, (double) pos.getZ() - targetPos.z());
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, this.speed, 0));
    }

    protected void tick(ServerWorld worldIn, E owner, long gameTime)
    {
        List<LivingEntity> targettableEntities = worldIn.getNearbyEntities(LivingEntity.class, this.entityPredicate, owner, owner.getBoundingBox());
        Brain<?> brain = owner.getBrain();
        if (!targettableEntities.isEmpty())
        {
            LivingEntity entity = targettableEntities.get(0);
            entity.hurt(DamageSource.mobAttack(owner), (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
            int speed = owner.hasEffect(Effects.MOVEMENT_SPEED)
                        ? owner.getEffect(Effects.MOVEMENT_SPEED).getAmplifier() + 1
                        : 0;
            int slowness = owner.hasEffect(Effects.MOVEMENT_SLOWDOWN)
                           ? owner.getEffect(Effects.MOVEMENT_SLOWDOWN).getAmplifier() + 1
                           : 0;
            float speedModifiers = 0.25F * (float) (speed - slowness);
            float speedStrength = MathHelper.clamp(owner.getSpeed() * 1.65F, 0.2F, 3.0F) + speedModifiers;
            float damageAmount = entity.isBlocking() ? 0.5F : 1.0F;
            entity.knockback((float) ((double) (damageAmount * speedStrength) * this.strengthMultiplier.applyAsDouble(owner)), this.direction.x(), this.direction.z());
            this.finishRam(worldIn, owner);
            worldIn.playSound(null, owner, this.sound.apply(owner), SoundCategory.HOSTILE, 1.0F, 1.0F);
        }
        else
        {
            Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
            Optional<Vector3d> ramTarget = brain.getMemory((MemoryModuleType) MemoryInit.RAM_TARGET.get());
            boolean isRamExpired = !walkTarget.isPresent() || !ramTarget.isPresent() || ((WalkTarget) walkTarget.get()).getTarget().currentPosition().distanceTo((Vector3d) ramTarget.get()) < 0.25;
            if (isRamExpired)
            {
                this.finishRam(worldIn, owner);
            }
        }
    }

    protected void finishRam(ServerWorld worldIn, E entityIn)
    {
        worldIn.broadcastEntityEvent(entityIn, (byte) 59);
        entityIn.getBrain().isMemoryValue((MemoryModuleType) MemoryInit.RAM_COOLDOWN_TICKS.get(), ((RangedInteger) this.cooldownRange.apply(entityIn)).randomValue(worldIn.getRandom()));
        entityIn.getBrain().eraseMemory((MemoryModuleType) MemoryInit.RAM_TARGET.get());
    }
}
