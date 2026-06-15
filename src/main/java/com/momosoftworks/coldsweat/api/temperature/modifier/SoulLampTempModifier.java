package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Function;

public class SoulLampTempModifier extends TempModifier
{
    @Override
    public Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        // 1.16 uses the entity's BURNING_POINT; in 1.7 that trait is an offset to the global MAX_TEMP.
        double almostMax = (ConfigSettings.MAX_TEMP.get() + Temperature.get(entity, Temperature.Type.BURNING_POINT)) * 0.99;
        double strength = ConfigSettings.SOULSPRING_LAMP_STRENGTH.get();
        return temp ->
        {
            if (temp < almostMax) return temp;
            return Math.max(temp * (1 - strength), almostMax);
        };
    }

    @Override
    public String getID()
    {   return "cold_sweat:soulspring_lamp";
    }
}
