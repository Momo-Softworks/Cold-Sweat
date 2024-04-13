package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class CuriosTempModifier extends TempModifier
{
    public CuriosTempModifier()
    {
        this(0d, 0d);
    }

    public CuriosTempModifier(double cold, double hot)
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
        {
            double insulation = temp > 0 ? hot : cold;
            return temp * (insulation >= 0 ? Math.pow(0.1, insulation / 60) : -(insulation / 20) + 1);
        };
    }

    public String getID()
    {
        return "curios:curios";
    }
}
