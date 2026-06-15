package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Function;

public class MountTempModifier extends TempModifier
{
    public MountTempModifier()
    {   this(0, 0);
    }

    public MountTempModifier(double coldInsul, double heatInsul)
    {   this.getNBT().setDouble("ColdInsulation", coldInsul);
        this.getNBT().setDouble("HeatInsulation", heatInsul);
    }

    @Override
    public Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        double insulationStrength = ConfigSettings.INSULATION_STRENGTH.get();

        return temp ->
        {
            double insulation = temp > 0
                                ? this.getNBT().getDouble("HeatInsulation")
                                : this.getNBT().getDouble("ColdInsulation");
            return CSMath.blend(temp, 0, insulation * insulationStrength, 0, 1);
        };
    }

    public String getID()
    {   return "cold_sweat:mount";
    }
}
