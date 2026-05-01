package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectParticleStorm;

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
        if (!entity.level().isClientSide())
        {
            BlockPos entityPos = WorldHelper.sublevelToWorld(entity.level(), entity.blockPosition());
            WeatherManagerServer weatherManager = ServerTickHandler.getWeatherManagerFor(entity.level().dimension());
            float windSpeed = weatherManager.getWindManager().getWindSpeedPositional(entityPos);

            WeatherObject weather = (WeatherObject) CompatManager.Weather2.getClosestStorm(entity.level(), entityPos);
            double stormTemp;
            // If there is a blizzard/sandstorm, apply the temperature and wind speed modifiers
            if (weather instanceof WeatherObjectParticleStorm storm)
            {
                double distance = storm.posGround.distanceTo(entity.position());

                if (storm.type == WeatherObjectParticleStorm.StormType.SANDSTORM)
                {   stormTemp = -CSMath.blend(0, storm.getIntensity() / 3, distance, storm.getSize(), 0);
                }
                else if (storm.type == WeatherObjectParticleStorm.StormType.SNOWSTORM)
                {   stormTemp = CSMath.blend(0, storm.getIntensity(), distance, storm.getSize(), 0);
                }
                else
                {   stormTemp = 0;
                }
            }
            else if (weather instanceof StormObject storm
            && storm.levelCurIntensityStage >= StormObject.STATE_FORMING)
            {
                double distance = storm.posGround.distanceTo(entity.position());
                stormTemp = CSMath.blend(0, storm.strength / 300, distance, storm.getSize(), 0);
            }
            else
            {   stormTemp = 0;
            }
            return temp -> temp - stormTemp - windSpeed / 5;
        }
        return temp -> temp;
    }
}
