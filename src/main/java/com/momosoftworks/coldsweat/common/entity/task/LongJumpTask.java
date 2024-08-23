package com.momosoftworks.coldsweat.common.entity.task;

import com.google.common.collect.ImmutableMap;
import com.momosoftworks.coldsweat.core.init.MemoryInit;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.potion.Effects;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class LongJumpTask<E extends MobEntity> extends Task<E>
{
    private final RangedInteger cooldownRange;
    private final int verticalRange;
    private final int horizontalRange;
    private final float maxRange;
    private final List<LongJumpTask.Target> targets = new ArrayList();
    private Optional<Vector3d> lastPos = Optional.empty();
    private Optional<LongJumpTask.Target> lastTarget = Optional.empty();
    private int cooldown;
    private long targetTime;
    private final Function<E, SoundEvent> sound;

    public LongJumpTask(RangedInteger cooldownRange, int verticalRange, int horizontalRange, float maxRange, Function<E, SoundEvent> sound)
    {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryInit.LONG_JUMP_COOLING_DOWN.get(), MemoryModuleStatus.VALUE_ABSENT, MemoryInit.LONG_JUMP_MID_JUMP.get(), MemoryModuleStatus.VALUE_ABSENT));
        this.cooldownRange = cooldownRange;
        this.verticalRange = verticalRange;
        this.horizontalRange = horizontalRange;
        this.maxRange = maxRange;
        this.sound = sound;
    }

    protected boolean checkExtraStartConditions(ServerWorld worldIn, E owner)
    {
        return owner.isOnGround() && !worldIn.getBlockState(owner.blockPosition()).is(Blocks.HONEY_BLOCK);
    }

    protected boolean canStillUse(ServerWorld worldIn, E entityIn, long gameTimeIn)
    {
        boolean shouldContinue = this.lastPos.isPresent() && ((Vector3d) this.lastPos.get()).equals(entityIn.position()) && this.cooldown > 0 && (this.lastTarget.isPresent() || !this.targets.isEmpty());
        if (!shouldContinue && !entityIn.getBrain().getMemory((MemoryModuleType) MemoryInit.LONG_JUMP_MID_JUMP.get()).isPresent())
        {
            entityIn.getBrain().setMemory((MemoryModuleType) MemoryInit.LONG_JUMP_COOLING_DOWN.get(), this.cooldownRange.randomValue(worldIn.getRandom()) / 2);
        }

        return shouldContinue;
    }

    protected void start(ServerWorld worldIn, E entityIn, long gameTimeIn)
    {
        this.lastTarget = Optional.empty();
        this.cooldown = 20;
        this.targets.clear();
        this.lastPos = Optional.of(entityIn.position());
        BlockPos pos = entityIn.blockPosition();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        Iterable<BlockPos> positions = BlockPos.betweenClosed(x - this.horizontalRange, y - this.verticalRange, z - this.horizontalRange, x + this.horizontalRange, y + this.verticalRange, z + this.horizontalRange);
        PathNavigator pathNavigator = entityIn.getNavigation();
        Iterator<BlockPos> var11 = positions.iterator();

        while (true)
        {
            BlockPos blockPos;
            double distance;
            do
            {
                if (!var11.hasNext())
                {   return;
                }

                blockPos = var11.next();
                distance = blockPos.distSqr(pos);
            }
            while (x == blockPos.getX() && z == blockPos.getZ());

            if (pathNavigator.isStableDestination(blockPos) && entityIn.getPathfindingMalus(WalkNodeProcessor.getBlockPathTypeStatic(entityIn.level, blockPos.mutable())) == 0.0F)
            {
                Optional<Vector3d> rammingVelocity = this.getRammingVelocity(entityIn, Vector3d.atCenterOf(blockPos));
                BlockPos finalBlockPos = blockPos;
                double finalDistance = distance;
                rammingVelocity.ifPresent((vector) ->
                {   this.targets.add(new LongJumpTask.Target(new BlockPos(finalBlockPos), vector, MathHelper.ceil(finalDistance)));
                });
            }
        }
    }

    protected void tick(ServerWorld worldIn, E owner, long gameTime)
    {
        if (this.lastTarget.isPresent())
        {
            if (gameTime - this.targetTime >= 40L)
            {
                owner.yRot = owner.yBodyRot;
                Vector3d vec3d = (this.lastTarget.get()).getRammingVelocity();
                double d = vec3d.length();
                double e = d + (owner.hasEffect(Effects.JUMP)
                                ? (double) (0.1F * (float) owner.getEffect(Effects.JUMP).getAmplifier() + 1.0F)
                                : 0.0);
                owner.setDeltaMovement(vec3d.scale(e / d));
                owner.getBrain().setMemory(MemoryInit.LONG_JUMP_MID_JUMP.get(), true);
                worldIn.playSound(null, owner, this.sound.apply(owner), SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
        }
        else
        {
            --this.cooldown;
            Optional<LongJumpTask.Target> optional = Optional.of(WeightedRandom.getRandomItem(worldIn.getRandom(), this.targets));
            if (optional.isPresent())
            {
                this.targets.remove(optional.get());
                owner.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper((optional.get()).getPos()));
                PathNavigator entityNavigation = owner.getNavigation();
                Path path = entityNavigation.createPath((optional.get()).getPos(), 0);
                if (path == null || !path.canReach())
                {
                    this.lastTarget = optional;
                    this.targetTime = gameTime;
                }
            }
        }

    }

    private Optional<Vector3d> getRammingVelocity(MobEntity entity, Vector3d pos)
    {
        Optional<Vector3d> velocity = Optional.empty();

        for (int range = 65; range < 85; range += 5)
        {
            Optional<Vector3d> rammingVelocity = this.getRammingVelocity(entity, pos, range);
            if (!velocity.isPresent() || rammingVelocity.isPresent() && ((Vector3d) rammingVelocity.get()).lengthSqr() < ((Vector3d) velocity.get()).lengthSqr())
            {   velocity = rammingVelocity;
            }
        }

        return velocity;
    }

    private Optional<Vector3d> getRammingVelocity(MobEntity entity, Vector3d pos, int range)
    {
        Vector3d vec3d = entity.position();
        Vector3d vec3d2 = (new Vector3d(pos.x - vec3d.x, 0.0, pos.z - vec3d.z)).normalize().scale(0.5);
        pos = pos.subtract(vec3d2);
        Vector3d vec3d3 = pos.subtract(vec3d);
        float f = (float) range * 3.1415927F / 180.0F;
        double d = Math.atan2(vec3d3.z, vec3d3.x);
        double e = vec3d3.subtract(0.0, vec3d3.y, 0.0).lengthSqr();
        double g = Math.sqrt(e);
        double h = vec3d3.y;
        double i = Math.sin((double) (2.0F * f));
        double k = Math.pow(Math.cos((double) f), 2.0);
        double l = Math.sin((double) f);
        double m = Math.cos((double) f);
        double n = Math.sin(d);
        double o = Math.cos(d);
        double p = e * 0.8 / (g * i - 2.0 * h * k);
        if (p < 0.0)
        {
            return Optional.empty();
        }
        else
        {
            double rangeIn = Math.sqrt(p);
            if (rangeIn > (double) this.maxRange)
            {
                return Optional.empty();
            }
            else
            {
                double r = rangeIn * m;
                double s = rangeIn * l;
                int t = MathHelper.ceil(g / r) * 2;
                double u = 0.0;
                Vector3d startPos = null;

                for (int v = 0; v < t - 1; ++v)
                {
                    u += g / (double) t;
                    double w = l / m * u - Math.pow(u, 2.0) * 0.08 / (2.0 * p * Math.pow(m, 2.0));
                    double x = u * o;
                    double y = u * n;
                    Vector3d endPos = new Vector3d(vec3d.x + x, vec3d.y + w, vec3d.z + y);
                    if (startPos != null && !this.canReach(entity, startPos, endPos))
                    {
                        return Optional.empty();
                    }

                    startPos = endPos;
                }

                return Optional.of((new Vector3d(r * o, s, r * n)).scale(0.949999988079071));
            }
        }
    }

    private boolean canReach(MobEntity entity, Vector3d startPos, Vector3d endPos)
    {
        EntitySize entityDimensions = entity.getDimensions(Pose.SPIN_ATTACK);
        Vector3d vec3d = endPos.subtract(startPos);
        double d = (double) Math.min(entityDimensions.width, entityDimensions.height);
        int i = MathHelper.ceil(vec3d.length() / d);
        Vector3d vec3d2 = vec3d.normalize();
        Vector3d vec3d3 = startPos;

        for (int j = 0; j < i; ++j)
        {
            vec3d3 = j == i - 1 ? endPos : vec3d3.add(vec3d2.scale(d * 0.8999999761581421));
            AxisAlignedBB box = entityDimensions.makeBoundingBox(vec3d3);
            if (!entity.level.noCollision(entity, box))
            {
                return false;
            }
        }

        return true;
    }

    public static class Target extends WeightedRandom.Item
    {
        private final BlockPos pos;
        private final Vector3d ramVelocity;

        public Target(BlockPos pos, Vector3d ramVelocity, int itemWeightIn)
        {
            super(itemWeightIn);
            this.pos = pos;
            this.ramVelocity = ramVelocity;
        }

        public BlockPos getPos()
        {
            return this.pos;
        }

        public Vector3d getRammingVelocity()
        {
            return this.ramVelocity;
        }
    }
}
