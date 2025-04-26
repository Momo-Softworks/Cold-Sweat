package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class EntityClimateTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        double worldTemp = WorldHelper.getRoughTemperatureAt(entity.level, entity.blockPosition());
        return temp -> temp + worldTemp;
    }
}
