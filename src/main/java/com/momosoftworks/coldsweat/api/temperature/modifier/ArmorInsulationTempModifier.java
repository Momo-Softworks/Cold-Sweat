package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
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
        double insulationStrength = ConfigSettings.INSULATION_STRENGTH.get();

        return temp ->
        {   double insulation = (temp > 0 ? hot : cold) * insulationStrength;
            if (insulation >= 0)
            {   return temp * Math.pow(0.1, insulation / 40);
            }
            else return temp * (-insulation / 20 + 1);
        };
    }
}