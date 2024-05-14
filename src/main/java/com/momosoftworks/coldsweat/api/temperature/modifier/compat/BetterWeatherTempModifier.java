package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import corgitaco.betterweather.api.season.Season;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

/**
 * Special TempModifier class for Serene Seasons
 */
public class BetterWeatherTempModifier extends TempModifier
{
    public BetterWeatherTempModifier() {}

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        Season season;
        if (entity.level.dimensionType().natural() && (season = Season.getSeason(entity.level)) != null)
        {
            double seasonEffect = 0;
            switch (season.getKey())
            {
                case AUTUMN:
                {
                    switch (season.getPhase())
                    {
                        case START: seasonEffect = ConfigSettings.AUTUMN_TEMPS.get()[0]; break;
                        case MID:   seasonEffect = ConfigSettings.AUTUMN_TEMPS.get()[1]; break;
                        case END:   seasonEffect = ConfigSettings.AUTUMN_TEMPS.get()[2]; break;
                    }
                    break;
                }

                case WINTER:
                {
                    switch (season.getPhase())
                    {
                        case START: seasonEffect = ConfigSettings.WINTER_TEMPS.get()[0]; break;
                        case MID:   seasonEffect = ConfigSettings.WINTER_TEMPS.get()[1]; break;
                        case END:   seasonEffect = ConfigSettings.WINTER_TEMPS.get()[2]; break;
                    }
                    break;
                }

                case SPRING:
                {
                    switch (season.getPhase())
                    {
                        case START: seasonEffect = ConfigSettings.SPRING_TEMPS.get()[0]; break;
                        case MID:   seasonEffect = ConfigSettings.SPRING_TEMPS.get()[1]; break;
                        case END:   seasonEffect = ConfigSettings.SPRING_TEMPS.get()[2]; break;
                    }
                    break;
                }

                case SUMMER:
                {
                    switch (season.getPhase())
                    {
                        case START: seasonEffect = ConfigSettings.SUMMER_TEMPS.get()[0]; break;
                        case MID:   seasonEffect = ConfigSettings.SUMMER_TEMPS.get()[1]; break;
                        case END:   seasonEffect = ConfigSettings.SUMMER_TEMPS.get()[2]; break;
                    }
                    break;
                }
            }

            double finalSeasonEffect = seasonEffect;
            return temp -> temp + finalSeasonEffect;
        }
        else
        {   return temp -> temp;
        }
    }
}
