package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class FreezingTempModifier extends TempModifier
{
    public FreezingTempModifier(double chill)
    {
        this.getNBT().putDouble("chill", chill);
    }

    public FreezingTempModifier()
    {
        this(0);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        return temp -> temp - this.getNBT().getDouble("chill");
    }

    @Override
    public String getID() {
        return "cold_sweat:freezing";
    }
}
