package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class FireTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        return temp -> entity.isOnFire() ? temp + 10 : temp;
    }

    @Override
    public String getID()
    {
        return "cold_sweat:on_fire";
    }
}
