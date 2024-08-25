package com.momosoftworks.coldsweat.common.entity.goal;

import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import com.momosoftworks.coldsweat.common.entity.data.edible.ChameleonEdibles;
import com.momosoftworks.coldsweat.common.entity.data.edible.Edible;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EatObjectsGoal extends Goal
{
    List<EntityType<?>> wantedEntities;
    ChameleonEntity entity;
    Entity target;
    boolean stoppedTasks;
    Vector3d lookPos = null;

    public EatObjectsGoal(ChameleonEntity chameleon, List<EntityType<?>> wantedEntities)
    {   this.wantedEntities = wantedEntities;
        this.entity = chameleon;
    }

    public boolean canContinueToUse()
    {   return (this.target != null && this.target.isAlive()) || this.entity.getEatTimer() > 0;
    }

    public boolean isInterruptable()
    {
        return false;
    }

    public void start()
    {
        // Scan for ItemEntity in a 5-block range
        List<Entity> items = this.entity.level.getEntities(this.entity, new AxisAlignedBB(this.entity.blockPosition()).inflate(5));
        for (Entity ent : items)
        {
            if (ent instanceof ItemEntity && ((ItemEntity) ent).getThrower() != null
            && (this.entity.isPlayerTrusted(((ItemEntity) ent).getThrower()) || this.entity.isTamingItem(((ItemEntity) ent).getItem())))
            {
                ItemEntity itemEntity = (ItemEntity) ent;
                ItemStack item = itemEntity.getItem();
                Optional<Edible> edible = ChameleonEdibles.getEdible(item);
                if (edible.isPresent()
                && (!itemEntity.getPersistentData().contains("Recipient") || itemEntity.getPersistentData().getUUID("Recipient").equals(this.entity.getUUID())))
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
        if ((this.target == null || !this.target.isAlive()) && this.entity.getEatTimer() <= 0)
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
            PathNavigator navigator = this.entity.getNavigation();
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
                WorldHelper.playEntitySound(ModSounds.CHAMELEON_TONGUE_OUT, this.entity, this.entity.getSoundSource(), 1, (float) Math.random() * 0.2f + 0.9f);

                // Remove target item
                TaskScheduler.scheduleServer(() ->
                {
                    if (this.target != null && this.target.isAlive())
                    {
                        this.entity.onEatEntity(this.target);
                        this.target.remove();

                        if (this.target instanceof ItemEntity)
                        {   ItemEntity item = (ItemEntity) this.target;
                            this.entity.onItemPickup(item);

                            UUID thrower = item.getThrower();
                            if (thrower != null)
                            {
                                if (item.getItem().getCount() > 0)
                                {   ItemStack stack = item.getItem().copy();
                                    stack.shrink(1);
                                    if (!stack.isEmpty())
                                    {   ItemEntity remainingStack = WorldHelper.entityDropItem(this.entity, stack);
                                        remainingStack.setThrower(thrower);
                                        remainingStack.getPersistentData().putUUID("Recipient", this.entity.getUUID());
                                    }
                                }
                            }
                        }

                        // Play tongue in sound
                        WorldHelper.playEntitySound(ModSounds.CHAMELEON_TONGUE_IN, this.entity, this.entity.getSoundSource(), 1, (float) Math.random() * 0.2f + 0.9f);
                    }
                }, this.entity.getEatAnimLength() / 2 + 2);

                // Send the entity toward the chameleon
                TaskScheduler.scheduleServer(() ->
                {
                    this.target.setDeltaMovement(this.entity.position().subtract(this.target.position()).normalize().scale(0.75));
                }, this.entity.getEatAnimLength() / 2);
            }

            this.entity.goalSelector.getRunningGoals().forEach(goal ->
            {
                Goal g = goal.getGoal();
                if (g instanceof TemptGoal || g instanceof LookAtGoal || g instanceof LookRandomlyGoal || g instanceof LazyLookGoal)
                {   this.stoppedTasks = true;
                    goal.stop();
                }
            });
        }
    }

    @Override
    public boolean canUse()
    {
        return this.entity.getLastHurtByMob() == null;
    }

    private boolean isBreedingItem(ItemStack stack)
    {
        return this.entity.canFallInLove() && this.entity.isFood(stack);
    }
}
