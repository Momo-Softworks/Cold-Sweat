package com.momosoftworks.coldsweat.compat.kubejs.util;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

/**
 * A TempModifier whose calculation is defined by a KubeJS script.
 */
public class TempModifierJS extends TempModifier
{
    private final Function<TempModifierDataJS, Function<Double, Double>> constructor;

    public TempModifierJS(Function<TempModifierDataJS, Function<Double, Double>> constructor)
    {   this.constructor = constructor;
    }

    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {   return constructor.apply(new TempModifierDataJS(entity, trait));
    }
}
