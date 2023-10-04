package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Function;

public class FireTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {   return temp -> entity.isBurning() ? temp + 10 : temp;
    }

    @Override
    public String getID()
    {
        return "cold_sweat:on_fire";
    }
}
