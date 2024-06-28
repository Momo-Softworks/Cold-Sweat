package com.momosoftworks.coldsweat.common.entity.goals;

import com.momosoftworks.coldsweat.common.entity.Chameleon;
import com.momosoftworks.coldsweat.common.entity.data.edible.ChameleonEdibles;
import com.momosoftworks.coldsweat.common.entity.data.edible.Edible;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ModSounds;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class EatObjectsGoal extends Goal
{
    List<EntityType<?>> wantedEntities;
    Chameleon entity;
    Entity target;
    boolean stoppedTasks;
    Vec3 lookPos = null;

    public EatObjectsGoal(Chameleon chameleon, List<EntityType<?>> wantedEntities)
    {
        this.wantedEntities = wantedEntities;
        this.entity = chameleon;
    }

    public boolean canContinueToUse()
    {
        return (this.target != null && this.target.isAlive() && !this.target.isRemoved()) || this.entity.getEatTimer() > 0;
    }

    public boolean isInterruptable()
    {
        return false;
    }

    public void start()
    {
        // Scan for ItemEntity in a 5-block range
        List<Entity> items = this.entity.level().getEntities(this.entity, new AABB(this.entity.blockPosition()).inflate(5));
        for (Entity ent : items)
        {
            if (ent instanceof ItemEntity itemEntity && itemEntity.getOwner() != null
            && (this.entity.isPlayerTrusted(itemEntity.getOwner().getUUID()) || this.entity.isTamingItem(itemEntity.getItem())))
            {
                ItemStack item = itemEntity.getItem();
                Optional<Edible> edible = ChameleonEdibles.getEdible(item);
                String recipient = NBTHelper.getTagOrEmpty(item).getString("Recipient");
                if (edible.isPresent()
                && (recipient.isEmpty() || recipient.equals(this.entity.getUUID().toString())))
                {
                    if (this.entity.getCooldown(edible.get()) <= 0 && edible.get().shouldEat(this.entity, itemEntity)
                    || isBreedingItem(itemEntity.getItem()))
                    {
                        this.target = ent;
                        this.lookPos = ent.position();
                        break;
                    }
                }
            }
            else if (this.wantedEntities.contains(ent.getType()))
            {
                this.target = ent;
                this.lookPos = ent.position();
                break;
            }
        }
    }

    public void stop()
    {
        this.stoppedTasks = false;
        this.target = null;
    }

    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    public void tick()
    {
        if ((this.target == null || this.target.isRemoved()) && this.entity.getEatTimer() <= 0)
        {
            this.stop();
            return;
        }

        // Look at target position
        this.lookPos = this.target.position().add(this.target.getDeltaMovement());
        this.entity.getLookControl().setLookAt(lookPos);

        if (this.entity.getEatTimer() <= 0)
        {
            // Move to the target
            PathNavigation navigator = this.entity.getNavigation();
            Path path = navigator.createPath(target, 0);
            if (path != null) navigator.moveTo(path, 1.5);

            // Update look position

            // If within 1.5 blocks, eat the item
            if (Math.sqrt(this.entity.distanceToSqr(this.target)) < 1.5 && Math.abs(this.entity.getY() - this.target.getY()) < 1 && this.target.isAlive())
            {
                navigator.stop();
                this.entity.getLookControl().setLookAt(lookPos);

                // Play tongue stretch animation
                this.entity.eatAnimation();

                // Play tongue out sound
                WorldHelper.playEntitySound(ModSounds.CHAMELEON_TONGUE_OUT.value(), this.entity, this.entity.getSoundSource(), 1, (float) Math.random() * 0.2f + 0.9f);

                // Remove target item
                TaskScheduler.scheduleServer(() ->
                {
                    if (this.target != null && this.target.isAlive() && !this.target.isRemoved())
                    {
                        this.entity.onEatEntity(this.target);
                        this.target.remove(Entity.RemovalReason.KILLED);

                        if (this.target instanceof ItemEntity item)
                        {   this.entity.onItemPickup(item);

                            Entity thrower = item.getOwner();
                            if (thrower != null)
                            {
                                if (item.getItem().getCount() > 0)
                                {   ItemStack stack = item.getItem().copy();
                                    stack.shrink(1);
                                    if (!stack.isEmpty())
                                    {   WorldHelper.entityDropItem(this.entity, stack).setThrower(thrower);
                                    }
                                }
                            }
                        }

                        // Play tongue in sound
                        WorldHelper.playEntitySound(ModSounds.CHAMELEON_TONGUE_IN.value(), this.entity, this.entity.getSoundSource(), 1, (float) Math.random() * 0.2f + 0.9f);
                    }
                }, this.entity.getEatAnimLength() / 2 + 2);

                // Send the entity toward the chameleon
                TaskScheduler.scheduleServer(() ->
                {
                    this.target.setDeltaMovement(this.entity.position().subtract(this.target.position()).normalize().scale(0.75));
                }, this.entity.getEatAnimLength() / 2);
            }

            for (WrappedGoal goal : this.entity.goalSelector.getAvailableGoals())
            {
                Goal g = goal.getGoal();
                if (g instanceof TemptGoal || g instanceof LookAtPlayerGoal || g instanceof RandomLookAroundGoal || g instanceof LazyLookGoal)
                {
                    if (g.isInterruptable())
                    {
                        this.stoppedTasks = true;
                        goal.stop();
                    }
                    else
                    {   stoppedTasks = false;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean canUse()
    {   return this.entity.getLastHurtByMob() == null;
    }

    private boolean isBreedingItem(ItemStack stack)
    {   return this.entity.canFallInLove() && this.entity.isFood(stack);
    }
}
