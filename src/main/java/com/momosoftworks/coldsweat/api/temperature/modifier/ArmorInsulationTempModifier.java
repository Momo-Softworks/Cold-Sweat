package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class ArmorInsulationTempModifier extends TempModifier
{
    public ArmorInsulationTempModifier()
    {
        this(0d, 0d);
    }

    public ArmorInsulationTempModifier(double cold, double hot)
    {
        this.getNBT().putDouble("cold", cold);
        this.getNBT().putDouble("hot", hot);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        double cold = this.getNBT().getDouble("cold");
        double hot = this.getNBT().getDouble("hot");

        return temp ->
        {   double insulation = temp > 0 ? hot : cold;
            return temp * (insulation >= 0 ? Math.pow(0.1, insulation / 60) : -(insulation / 20) + 1);
        };
    }
}