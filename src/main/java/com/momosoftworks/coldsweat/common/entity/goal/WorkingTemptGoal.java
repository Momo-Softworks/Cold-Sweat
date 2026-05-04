package com.momosoftworks.coldsweat.common.entity.goal;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * A tempt goal that actually works and uses tags, rather than Ingredients that initialize empty half the time.
 */
public class WorkingTemptGoal extends TemptGoal
{
    protected static final TargetingConditions TEMP_TARGETING = TargetingConditions.forNonCombat().range(10.0D).ignoreLineOfSight();

    protected final TagKey<Item> itemTag;
    protected final TargetingConditions targetingConditions;
    protected int calmDown = 0;
    protected boolean isRunning = false;

    public WorkingTemptGoal(PathfinderMob mob, double speedModifier, TagKey<Item> itemTag, boolean canScare)
    {
        super(mob, speedModifier, Ingredient.EMPTY, canScare);
        this.targetingConditions = TEMP_TARGETING.copy().selector(this::shouldFollow);
        this.itemTag = itemTag;
    }

    public boolean canUse()
    {
        if (this.calmDown > 0)
        {   this.calmDown--;
            return false;
        }
        else
        {   this.player = this.mob.level().getNearestPlayer(this.targetingConditions, this.mob);
            return this.player != null;
        }
    }

    protected boolean shouldFollow(LivingEntity entity)
    {   return entity.getMainHandItem().is(this.itemTag) || entity.getOffhandItem().is(this.itemTag);
    }

    @Override
    public void start()
    {   super.start();
        this.isRunning = true;
    }

    @Override
    public void stop()
    {   super.stop();
        this.isRunning = false;
    }

    @Override
    public boolean isRunning()
    {   return this.isRunning;
    }
}
