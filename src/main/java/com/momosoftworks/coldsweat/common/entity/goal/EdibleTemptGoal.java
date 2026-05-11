package com.momosoftworks.coldsweat.common.entity.goal;

import com.momosoftworks.coldsweat.common.entity.Chameleon;
import com.momosoftworks.coldsweat.common.entity.data.edible.ChameleonEdibles;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * A tempt goal based on registered {@link com.momosoftworks.coldsweat.common.entity.data.edible.Edible}s
 */
public class EdibleTemptGoal extends TemptGoal
{
    protected static final TargetingConditions TEMP_TARGETING = TargetingConditions.forNonCombat().range(10.0D).ignoreLineOfSight();

    protected final TargetingConditions targetingConditions;
    protected int calmDown = 0;
    protected boolean isRunning = false;
    protected Chameleon mob;

    public EdibleTemptGoal(Chameleon mob, double speedModifier, boolean canScare)
    {
        super(mob, speedModifier, Ingredient.EMPTY, canScare);
        this.mob = mob;
        this.targetingConditions = TEMP_TARGETING.copy().selector(this::shouldFollow);
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
    {
        return ChameleonEdibles.EDIBLES.stream().anyMatch(edible ->
               {
                   for (ItemStack stack : entity.getHandSlots())
                   {
                       if (!stack.is(edible.associatedItems())) continue;
                       boolean shouldEat = edible.shouldEat(stack, this.mob, entity);
                       if (shouldEat)
                       {   return true;
                       }
                   }
                   return false;
               });
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
