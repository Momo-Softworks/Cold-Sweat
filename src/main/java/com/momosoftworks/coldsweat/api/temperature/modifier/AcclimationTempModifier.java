package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Pair;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Function;

/**
 * Gradually shifts the entity's freezing/burning points toward the ambient temperature, so it
 * "acclimates" to its environment over time. Ported from 1.16's {@code AcclimationTempModifier}.<br>
 * Disabled when {@link ConfigSettings#ACCLIMATION_SPEED} is 0.
 */
public class AcclimationTempModifier extends TempModifier
{
    public AcclimationTempModifier() {}

    @Override
    protected Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode)
        {   return temp -> temp;
        }

        double minTemp = Temperature.get(entity, Temperature.Type.FREEZING_POINT);
        double maxTemp = Temperature.get(entity, Temperature.Type.BURNING_POINT);
        double tempFactor = CSMath.blend(-1, 1, Temperature.get(entity, Temperature.Type.WORLD), minTemp, maxTemp);
        double acclimateSpeed = (ConfigSettings.ACCLIMATION_SPEED.get() * this.getTickRate()) / 20;

        if (acclimateSpeed == 0) return temp -> temp;

        switch (type)
        {
            case FREEZING_POINT :
            {
                Pair<Double, Double> minRange = ConfigSettings.MIN_ACCLIMATION_RANGE.get();
                double minAcclimation = this.getNBT().getDouble("MinAcclimation");
                double delta = tempFactor < -0.5 ? -acclimateSpeed
                             : tempFactor <  0.5 ?  acclimateSpeed / 2
                             :                      acclimateSpeed;

                double lowerBound = minRange.getFirst();
                double upperBound = Math.max(minAcclimation, tempFactor > 0.5 ? minRange.getSecond() : 0);

                double newAcclimation = CSMath.clamp(minAcclimation + delta, lowerBound, upperBound);
                this.getNBT().setDouble("MinAcclimation", newAcclimation);

                final double offset = newAcclimation;
                return temp -> temp + offset;
            }
            case BURNING_POINT :
            {
                Pair<Double, Double> maxRange = ConfigSettings.MAX_ACCLIMATION_RANGE.get();
                double maxAcclimation = this.getNBT().getDouble("MaxAcclimation");
                double delta = tempFactor >  0.5 ?  acclimateSpeed
                             : tempFactor > -0.5 ? -acclimateSpeed / 2
                             :                     -acclimateSpeed;

                double lowerBound = Math.min(maxAcclimation, tempFactor < -0.5 ? maxRange.getFirst() : 0);
                double upperBound = maxRange.getSecond();

                double newAcclimation = CSMath.clamp(maxAcclimation + delta, lowerBound, upperBound);
                this.getNBT().setDouble("MaxAcclimation", newAcclimation);

                final double offset = newAcclimation;
                return temp -> temp + offset;
            }
            default :
            {   return temp -> temp;
            }
        }
    }

    public String getID()
    {   return "cold_sweat:acclimation";
    }
}
