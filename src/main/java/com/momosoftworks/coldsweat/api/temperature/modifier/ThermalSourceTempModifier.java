package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Function;

/**
 * Base class for the temperature effect applied by thermal sources (Hearth, Boiler, Icebox).<br>
 * Ported from 1.16's {@code ThermalSourceTempModifier}. Pulls the entity's world temperature toward the
 * habitable mid-point: warming raises it when cold, cooling lowers it when hot.<br>
 * Uses {@link ConfigSettings#HEARTH_EFFECT} as the source strength (1.16's {@code THERMAL_SOURCE_STRENGTH}).
 */
public abstract class ThermalSourceTempModifier extends TempModifier
{
    public ThermalSourceTempModifier(int cooling, int warming)
    {   this.getNBT().setInteger("Cooling", cooling);
        this.getNBT().setInteger("Warming", warming);
    }

    public abstract int getStrength();

    protected int getCooling()
    {   return this.getNBT().getInteger("Cooling");
    }

    protected int getWarming()
    {   return this.getNBT().getInteger("Warming");
    }

    @Override
    public Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        double min = ConfigSettings.MIN_TEMP.get();
        double max = ConfigSettings.MAX_TEMP.get();
        double mid = (min + max) / 2;
        double sourceStrength = ConfigSettings.HEARTH_EFFECT.get();

        double cooling = this.getCooling() * sourceStrength;
        double warming = this.getWarming() * sourceStrength;

        return temp ->
        {
            if (temp > mid)
            {   return CSMath.blend(temp, mid, cooling, 0, 10);
            }
            if (temp < mid)
            {   return CSMath.blend(temp, mid, warming, 0, 10);
            }
            return temp;
        };
    }
}
