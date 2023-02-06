package dev.momostudios.coldsweat.common.entity.goals;

import dev.momostudios.coldsweat.common.entity.Chameleon;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.List;

public class EatDroppedItemsGoal extends Goal
{
    List<Item> wantedItems;
    Chameleon entity;
    ItemEntity target;
    boolean stoppedTempting;

    public EatDroppedItemsGoal(Chameleon chameleon, List<Item> wantedItems)
    {
        this.entity = chameleon;
        this.wantedItems = wantedItems;
    }

    public boolean canContinueToUse()
    {
        return (this.target != null && this.target.isAlive() && !this.target.isRemoved()) || this.entity.getEatTimer() < this.entity.getEatAnimLength();
    }

    public boolean isInterruptable()
    {
        return !this.canUse() && this.entity.getEatTimer() >= this.entity.getEatAnimLength();
    }

    public void start()
    {
        // scan for ItemEntity in a 5-block range
        List<ItemEntity> items = this.entity.level.getEntitiesOfClass(ItemEntity.class, this.entity.getBoundingBox().inflate(5));
        for (ItemEntity item : items)
        {
            if (this.wantedItems.contains(item.getItem().getItem()))
            {
                this.target = item;
                break;
            }
        }
    }

    public void stop()
    {
        this.stoppedTempting = false;
        this.target = null;
    }

    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    public void tick()
    {
        if (this.target == null || this.target.isRemoved())
        {
            this.stop();
        }
        else if (this.entity.getEatTimer() >= this.entity.getEatAnimLength())
        {
            // Move to within 1.5 blocks of the ItemEntity
            this.entity.getNavigation().moveTo(this.target, 1.5);
            // Look at the target
            this.entity.lookAt(EntityAnchorArgument.Anchor.EYES, this.target.position().add(this.target.getDeltaMovement()));
            // If within 1.5 blocks, eat the item
            if (this.entity.distanceToSqr(this.target) < 2 && Math.abs(this.entity.getY() - this.target.getY()) < 1 && this.target.isAlive())
            {
                // Play tongue stretch animation
                this.entity.eatAnimation();
                // Remove target item
                TaskScheduler.scheduleServer(() ->
                {
                    if (this.target != null && this.target.isAlive() && !this.target.isRemoved())
                    {
                        this.target.remove(Entity.RemovalReason.KILLED);
                        Player player = this.target.getThrower() != null ? this.entity.level.getPlayerByUUID(this.target.getThrower()) : null;
                        if (player != null && !this.entity.isPlayerTrusted(player))
                        {
                            if (player.isCreative() || Math.random() < 0.3)
                            {
                                this.entity.setPersistenceRequired();
                                this.entity.addTrustedPlayer(this.target.getThrower());
                                WorldHelper.spawnParticleBatch(this.entity.level, ParticleTypes.HEART, this.entity.getX(), this.entity.getY() + 1, this.entity.getZ(), 1, 1, 1, 6, 0.01);
                            }
                            else
                            {
                                WorldHelper.spawnParticleBatch(this.entity.level, ParticleTypes.SMOKE, this.entity.getX(), this.entity.getY() + 1, this.entity.getZ(), 1, 1, 1, 6, 0.01);
                            }
                        }
                    }
                }, (int) (this.entity.getEatAnimLength() / 2));
            }

            if (!this.stoppedTempting)
            {
                this.entity.goalSelector.getRunningGoals().forEach(goal ->
                {
                    if (goal.getGoal() instanceof TemptGoal)
                    {
                        this.stoppedTempting = true;
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
