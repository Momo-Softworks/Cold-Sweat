package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class FoodTempModifier extends TempModifier
{
    int[] overridden = new int[Temperature.Trait.values().length];

    public FoodTempModifier()
    {
        this(0);
    }

    public FoodTempModifier(double temperature)
    {
        this.getNBT().putDouble("temperature", temperature);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        if (this.overridden[trait.ordinal()] > 0)
        {   return temp -> temp;
        }
        return temp -> temp + this.getNBT().getDouble("temperature");
    }

    private void updateOverridden(LivingEntity entity, Temperature.Trait trait, TempModifier sibling, boolean add)
    {
        double temperature = this.getNBT().getDouble("temperature");
        if (sibling instanceof FoodTempModifier)
        {
            double siblingTemp = sibling.getNBT().getDouble("temperature");
            if (CSMath.sign(temperature) == CSMath.sign(siblingTemp) && Math.abs(siblingTemp) > Math.abs(temperature))
            {   this.overridden[trait.ordinal()] += add ? 1 : -1;
            }
            this.update(this.getLastInput(trait), entity, trait);
        }
    }

    @Override
    public void onSiblingAdded(LivingEntity entity, Temperature.Trait trait, TempModifier sibling)
    {   this.updateOverridden(entity, trait, sibling, true);
    }

    @Override
    public void onSiblingRemoved(LivingEntity entity, Temperature.Trait trait, TempModifier sibling)
    {   this.updateOverridden(entity, trait, sibling, false);
    }
}
