package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class FreezingTempModifier extends TempModifier
{
    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        double freezeAmount = CSMath.blend(0, 20, entity.getTicksFrozen(), 0, entity.getTicksRequiredToFreeze());
        if (freezeAmount == 0)
        {   this.expires(0);
        }
        return temp -> temp - freezeAmount;
    }
}
