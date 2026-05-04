package com.momosoftworks.coldsweat.common.entity.goal;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;

import java.util.function.Supplier;

/**
 * A tempt goal that actually works and uses tags, rather than Ingredients that initialize empty half the time.
 */
public class WorkingTemptGoal extends TemptGoal
{
    protected static final Supplier<EntityPredicate> TEMP_TARGETING = () -> new EntityPredicate().range(10.0D).allowInvulnerable().allowSameTeam().allowNonAttackable().allowUnseeable();

    protected final ITag<Item> itemTag;
    protected EntityPredicate targetingConditions;
    protected int calmDown = 0;
    protected boolean isRunning = false;

    public WorkingTemptGoal(CreatureEntity mob, double speedModifier, ITag<Item> itemTag, boolean canScare)
    {
        super(mob, speedModifier, Ingredient.EMPTY, canScare);
        this.itemTag = itemTag;
        this.targetingConditions = TEMP_TARGETING.get().selector(this::shouldFollow);
    }

    public boolean canUse()
    {
        if (this.calmDown > 0)
        {   this.calmDown--;
            return false;
        }
        else
        {   this.player = this.mob.level.getNearestPlayer(this.targetingConditions, this.mob);
            return this.player != null;
        }
    }

    protected boolean shouldFollow(LivingEntity entity)
    {   return this.itemTag.contains(entity.getMainHandItem().getItem()) || this.itemTag.contains(entity.getOffhandItem().getItem());
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
