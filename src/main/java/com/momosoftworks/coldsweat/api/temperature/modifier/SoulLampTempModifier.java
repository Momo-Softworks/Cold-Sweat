package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class SoulLampTempModifier extends TempModifier
{
    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        double almostMax = ConfigSettings.MAX_TEMP.get() * 0.99;
        return temp ->
        {
            if (temp < almostMax) return temp;
            return Math.max(temp * 0.4, almostMax);
        };
    }
}
