package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class MountTempModifier extends TempModifier
{
    public MountTempModifier()
    {   this(0, 0);
    }

    public MountTempModifier(double coldInsul, double heatInsul)
    {   this.getNBT().putDouble("ColdInsulation", coldInsul);
        this.getNBT().putDouble("HeatInsulation", heatInsul);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        return temp ->
        {   String toChange = temp > 0 ? "HeatInsulation" : "ColdInsulation";
            return CSMath.blend(temp, 0, this.getNBT().getDouble(toChange), 0, 1);
        };
    }
}