package com.momosoftworks.coldsweat.common.entity.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.momosoftworks.coldsweat.core.init.MemoryInit;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public class PrepareRamTask<E extends CreatureEntity> extends Task<E>
{
    private final ToIntFunction<E> cooldown;
    private final int minRamDistance;
    private final int maxRamDistance;
    private final float speed;
    private final EntityPredicate entityPredicate;
    private final int prepareTime;
    private final Function<E, SoundEvent> sound;
    private Optional<Long> prepareStartTime = Optional.empty();
    private Optional<PrepareRamTask.Ram> ram = Optional.empty();

    public PrepareRamTask(ToIntFunction<E> cooldown, int minRamDistance, int maxRamDistance, float speed, EntityPredicate entityPredicate, int prepareTime, Function<E, SoundEvent> sound)
    {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryInit.RAM_COOLDOWN_TICKS.get(), MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.VALUE_PRESENT, MemoryInit.RAM_TARGET.get(), MemoryModuleStatus.VALUE_ABSENT), 160);
        this.cooldown = cooldown;
        this.minRamDistance = minRamDistance;
        this.maxRamDistance = maxRamDistance;
        this.speed = speed;
        this.entityPredicate = entityPredicate;
        this.prepareTime = prepareTime;
        this.sound = sound;
    }

    protected void start(ServerWorld worldIn, E entityIn, long gameTimeIn)
    {
        Brain<?> brain = entityIn.getBrain();
        brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).flatMap((mobs) -> {
            return mobs.stream().filter((mob) -> {
                return this.entityPredicate.test(entityIn, mob);
            }).findFirst();
        }).ifPresent((mob) -> {
            this.findRam(entityIn, mob);
        });
    }

    protected void stop(ServerWorld worldIn, E entityIn, long gameTimeIn)
    {
        Brain<?> brain = entityIn.getBrain();
        if (!brain.hasMemoryValue((MemoryModuleType) MemoryInit.RAM_TARGET.get()))
        {
            worldIn.broadcastEntityEvent(entityIn, (byte) 59);
            brain.setMemory((MemoryModuleType) MemoryInit.RAM_COOLDOWN_TICKS.get(), this.cooldown.applyAsInt(entityIn));
        }

    }

    protected boolean canStillUse(ServerWorld worldIn, E entityIn, long gameTimeIn)
    {
        return this.ram.isPresent() && (this.ram.get()).getEntity().isAlive();
    }

    protected void tick(ServerWorld worldIn, E entityIn, long gameTime)
    {
        if (this.ram.isPresent())
        {
            entityIn.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget((this.ram.get()).getStart(), this.speed, 0));
            entityIn.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper((this.ram.get()).getEntity(), true));
            boolean isTargeting = !((PrepareRamTask.Ram) this.ram.get()).getEntity().blockPosition().equals((this.ram.get()).getEnd());
            if (isTargeting)
            {
                worldIn.broadcastEntityEvent(entityIn, (byte) 59);
                entityIn.getNavigation().isDone();
                this.findRam(entityIn, ((PrepareRamTask.Ram) this.ram.get()).entity);
            }
            else
            {
                BlockPos pos = entityIn.blockPosition();
                if (pos.equals(((PrepareRamTask.Ram) this.ram.get()).getStart()))
                {
                    worldIn.broadcastEntityEvent(entityIn, (byte) 58);
                    if (!this.prepareStartTime.isPresent())
                    {
                        this.prepareStartTime = Optional.of(gameTime);
                    }

                    if (gameTime - (Long) this.prepareStartTime.get() >= (long) this.prepareTime)
                    {
                        entityIn.getBrain().setMemory(MemoryInit.RAM_TARGET.get(), this.calculateRamTarget(pos, ((PrepareRamTask.Ram) this.ram.get()).getEnd()));
                        worldIn.playSound(null, entityIn, this.sound.apply(entityIn), SoundCategory.HOSTILE, 1.0F, 1.0F);
                        this.ram = Optional.empty();
                    }
                }
            }
        }

    }

    private Vector3d calculateRamTarget(BlockPos start, BlockPos end)
    {
        double x = 0.5 * (double) MathHelper.sign((start.getX() - end.getX()));
        double z = 0.5 * (double) MathHelper.sign((start.getZ() - end.getZ()));
        return Vector3d.atBottomCenterOf(end).add(x, 0.0, z);
    }

    private Optional<BlockPos> findRamStart(CreatureEntity entity, LivingEntity target)
    {
        BlockPos pos = target.blockPosition();
        if (!this.canReach(entity, pos))
        {
            return Optional.empty();
        }
        else
        {
            List<BlockPos> positions = Lists.newArrayList();
            BlockPos.Mutable mutable = pos.mutable();
            Iterator var6 = Direction.Plane.HORIZONTAL.iterator();

            while (var6.hasNext())
            {
                Direction direction = (Direction) var6.next();
                mutable.set(pos);

                for (int distance = 0; distance < this.maxRamDistance; ++distance)
                {
                    if (!this.canReach(entity, mutable.move(direction)))
                    {
                        mutable.move(direction.getOpposite());
                        break;
                    }
                }

                if (mutable.distManhattan(pos) >= this.minRamDistance)
                {
                    positions.add(mutable.immutable());
                }
            }

            PathNavigator navigator = entity.getNavigation();
            Stream<BlockPos> validPositions = positions.stream();
            BlockPos entityPos = entity.blockPosition();
            entityPos.getClass();
            return validPositions.sorted(Comparator.comparingDouble(entityPos::distSqr)).filter((start) -> {
                Path path = navigator.createPath(start, 0);
                return path != null && path.canReach();
            }).findFirst();
        }
    }

    private boolean canReach(CreatureEntity entity, BlockPos target)
    {
        return entity.getNavigation().isStableDestination(target) && entity.getPathfindingMalus(WalkNodeProcessor.getBlockPathTypeStatic(entity.level, target.mutable())) == 0.0F;
    }

    private void findRam(CreatureEntity entity, LivingEntity target)
    {
        this.prepareStartTime = Optional.empty();
        this.ram = this.findRamStart(entity, target).map((start) -> {
            return new PrepareRamTask.Ram(start, target.blockPosition(), target);
        });
    }

    public static class Ram
    {
        private final BlockPos start;
        private final BlockPos end;
        private final LivingEntity entity;

        public Ram(BlockPos start, BlockPos end, LivingEntity entity)
        {
            this.start = start;
            this.end = end;
            this.entity = entity;
        }

        public BlockPos getStart()
        {
            return this.start;
        }

        public BlockPos getEnd()
        {
            return this.end;
        }

        public LivingEntity getEntity()
        {
            return this.entity;
        }
    }
}
