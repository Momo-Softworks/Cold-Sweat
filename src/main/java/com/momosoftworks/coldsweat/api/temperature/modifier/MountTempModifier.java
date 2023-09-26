package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class MountTempModifier extends TempModifier
{
    public MountTempModifier()
    {   this(0, 0);
    }

    public MountTempModifier(int warming, int cooling)
    {   this.getNBT().putDouble("Warming", warming);
        this.getNBT().putDouble("Cooling", cooling);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        return temp -> temp > 0
                     ? temp / (1 + this.getNBT().getDouble("Cooling"))
                     : temp / (1 + this.getNBT().getDouble("Warming"));
    }

    public String getID()
    {
        return "cold_sweat:mount";
    }
}