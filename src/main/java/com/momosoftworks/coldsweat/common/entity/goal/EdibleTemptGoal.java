package com.momosoftworks.coldsweat.common.entity.goal;

import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import com.momosoftworks.coldsweat.common.entity.data.edible.ChameleonEdibles;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;

import java.util.function.Supplier;

/**
 * A tempt goal based on registered {@link com.momosoftworks.coldsweat.common.entity.data.edible.Edible}s
 */
public class EdibleTemptGoal extends TemptGoal
{
    protected static final Supplier<EntityPredicate> TEMP_TARGETING = () -> new EntityPredicate().range(10.0D).allowInvulnerable().allowSameTeam().allowNonAttackable().allowUnseeable();

    protected final EntityPredicate targetingConditions;
    protected int calmDown = 0;
    protected boolean isRunning = false;
    protected ChameleonEntity mob;

    public EdibleTemptGoal(ChameleonEntity mob, double speedModifier, boolean canScare)
    {
        super(mob, speedModifier, Ingredient.EMPTY, canScare);
        this.mob = mob;
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
    {
        return ChameleonEdibles.EDIBLES.stream().anyMatch(edible ->
               {
                   for (ItemStack stack : entity.getHandSlots())
                   {
                       if (!edible.associatedItems().contains(stack.getItem())) continue;
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
