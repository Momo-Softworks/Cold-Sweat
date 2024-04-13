package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

/**
 * Special TempModifier class for Weather 2
 */
public class StormTempModifier extends TempModifier
{
    public StormTempModifier() {}

    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        /*if (!entity.level.isClientSide())
        {
            WeatherManagerServer weatherManager = ServerTickHandler.getWeatherManagerFor(entity.level.dimension());
            WeatherObjectParticleStorm snowStorm = weatherManager.getClosestParticleStormByIntensity(entity.position(), WeatherObjectParticleStorm.StormType.SNOWSTORM);
            if (snowStorm != null)
            {
                double distance = snowStorm.pos.distanceTo(entity.position());
                if (distance > snowStorm.getSize())
                    return temp -> temp;
                return temp -> temp - CSMath.blend(0.5, 0, distance, 100, snowStorm.getSize()) * snowStorm.getIntensity();
            }
        }*/
        return temp -> temp;
    }

    @Override
    public String getID()
    {
        return "weather2:storm";
    }
}
