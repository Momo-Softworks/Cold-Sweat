package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Function;

public class InsulationTempModifier extends TempModifier
{
    public InsulationTempModifier()
    {
        this(0d, 0d);
    }

    public InsulationTempModifier(double cold, double hot)
    {
        this.getNBT().setDouble("cold", cold);
        this.getNBT().setDouble("hot", hot);
    }

    @Override
    public Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        double cold = this.getNBT().getDouble("cold");
        double hot = this.getNBT().getDouble("hot");

        return temp ->
        {   double insulation = temp > 0 ? hot : cold;
            return temp * (insulation >= 0 ? Math.pow(0.1, insulation / 60) : -(insulation / 20) + 1);
        };
    }

    public String getID()
    {
        return "cold_sweat:armor";
    }
}