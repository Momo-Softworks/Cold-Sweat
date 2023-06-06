package dev.momostudios.coldsweat.api.temperature.modifier.compat;

import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

/**
 * Special TempModifier class for Weather 2
 */
public class StormTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
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
