package dev.momostudios.coldsweat.common.entity.goals;

import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import dev.momostudios.coldsweat.common.entity.data.edible.ChameleonEdibles;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

public class EatObjectsGoal extends Goal
{
    List<Item> wantedItems;
    List<EntityType<?>> wantedEntities;
    ChameleonEntity entity;
    Entity target;
    boolean stoppedTasks;
    Vec3 lookPos = null;

    public EatObjectsGoal(ChameleonEntity chameleon, List<Item> wantedItems, List<EntityType<?>> wantedEntities)
    {
        this.wantedItems = wantedItems;
        this.wantedEntities = wantedEntities;
        this.entity = chameleon;
        this.wantedItems.addAll(ChameleonEdibles.EDIBLES.keySet().stream().toList());
        this.wantedItems.addAll(ConfigSettings.CHAMELEON_TAME_ITEMS.get().keySet());
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
        // scan for ItemEntity in a 5-block range
        List<Entity> items = this.entity.level.getEntities(this.entity, this.entity.getBoundingBox().inflate(5));
        for (Entity ent : items)
        {
            if (ent instanceof ItemEntity itemEntity && itemEntity.getThrower() != null)
            {
                Item item = itemEntity.getItem().getItem();
                if (this.wantedItems.contains(item) && this.entity.getCooldown(item) <= 0)
                {
                    this.target = ent;
                    this.lookPos = ent.position();
                    break;
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
        this.entity.getLookControl().setLookAt(lookPos);

        if (this.entity.getEatTimer() <= 0)
        {
            // Move to within 1.5 blocks of the ItemEntity
            this.entity.getNavigation().moveTo(this.target, 1.5);

            // Update look position
            this.lookPos = this.target.position().add(this.target.getDeltaMovement());

            // If within 1.5 blocks, eat the item
            if (this.entity.distanceToSqr(this.target) < 2 && Math.abs(this.entity.getY() - this.target.getY()) < 1 && this.target.isAlive())
            {
                // Play tongue stretch animation
                this.entity.eatAnimation();

                // Play tongue out sound
                WorldHelper.playEntitySound(ModSounds.CHAMELEON_TONGUE_OUT, this.entity, this.entity.getSoundSource(), 1, (float) Math.random() * 0.2f + 0.9f);

                // Remove target item
                TaskScheduler.scheduleServer(() ->
                {
                    if (this.target != null && this.target.isAlive() && !this.target.isRemoved())
                    {
                        this.entity.onEatEntity(this.target);
                        this.target.remove(Entity.RemovalReason.KILLED);
                        if (this.target instanceof ItemEntity item)
                        {   this.entity.onItemPickup(item);
                        }

                        // Play tongue in sound
                        WorldHelper.playEntitySound(ModSounds.CHAMELEON_TONGUE_IN, this.entity, this.entity.getSoundSource(), 1, (float) Math.random() * 0.2f + 0.9f);
                    }
                }, this.entity.getEatAnimLength() - 1);

                // Send the entity toward the chameleon
                TaskScheduler.scheduleServer(() ->
                {
                    this.target.setDeltaMovement(this.entity.position().subtract(this.target.position()).normalize().scale(0.3));
                }, this.entity.getEatAnimLength() / 2);
            }

            if (!this.stoppedTasks)
            {
                this.entity.goalSelector.getRunningGoals().forEach(goal ->
                {
                    Goal g = goal.getGoal();
                    if (g instanceof TemptGoal || g instanceof LookAtPlayerGoal || g instanceof RandomLookAroundGoal)
                    {
                        this.stoppedTasks = true;
                        goal.stop();
                    }
                });
            }
        }
    }

    @Override
    public boolean canUse()
    {
        return this.entity.getLastHurtByMob() == null;
    }
}
