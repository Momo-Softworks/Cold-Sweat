package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.LivingEntity;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;

import java.util.function.Function;

/**
 * Special TempModifier class for Serene Seasons
 */
public class SereneSeasonsTempModifier extends TempModifier
{
    public SereneSeasonsTempModifier() {}

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        if (entity.level.dimensionType().natural())
        {
            ISeasonState season = SeasonHelper.getSeasonState(entity.level);
            double startValue;
            double endValue;

            switch (season.getSubSeason())
            {
                case EARLY_AUTUMN : { startValue = ConfigSettings.AUTUMN_TEMPS.get()[0]; endValue = ConfigSettings.AUTUMN_TEMPS.get()[1]; break; }
                case MID_AUTUMN   : { startValue = ConfigSettings.AUTUMN_TEMPS.get()[1]; endValue = ConfigSettings.AUTUMN_TEMPS.get()[2]; break; }
                case LATE_AUTUMN  : { startValue = ConfigSettings.AUTUMN_TEMPS.get()[2]; endValue = ConfigSettings.WINTER_TEMPS.get()[0]; break; }

                case EARLY_WINTER : { startValue = ConfigSettings.WINTER_TEMPS.get()[0]; endValue = ConfigSettings.WINTER_TEMPS.get()[1]; break; }
                case MID_WINTER   : { startValue = ConfigSettings.WINTER_TEMPS.get()[1]; endValue = ConfigSettings.WINTER_TEMPS.get()[2]; break; }
                case LATE_WINTER  : { startValue = ConfigSettings.WINTER_TEMPS.get()[2]; endValue = ConfigSettings.SPRING_TEMPS.get()[0]; break; }

                case EARLY_SPRING : { startValue = ConfigSettings.SPRING_TEMPS.get()[0]; endValue = ConfigSettings.SPRING_TEMPS.get()[1]; break; }
                case MID_SPRING   : { startValue = ConfigSettings.SPRING_TEMPS.get()[1]; endValue = ConfigSettings.SPRING_TEMPS.get()[2]; break; }
                case LATE_SPRING  : { startValue = ConfigSettings.SPRING_TEMPS.get()[2]; endValue = ConfigSettings.SUMMER_TEMPS.get()[0]; break; }

                case EARLY_SUMMER : { startValue = ConfigSettings.SUMMER_TEMPS.get()[0]; endValue = ConfigSettings.SUMMER_TEMPS.get()[1]; break; }
                case MID_SUMMER   : { startValue = ConfigSettings.SUMMER_TEMPS.get()[1]; endValue = ConfigSettings.SUMMER_TEMPS.get()[2]; break; }
                case LATE_SUMMER  : { startValue = ConfigSettings.SUMMER_TEMPS.get()[2]; endValue = ConfigSettings.AUTUMN_TEMPS.get()[0]; break; }

                default : { return temp -> temp; }

            }
            return temp -> temp + (float) CSMath.blend(startValue, endValue, season.getDay() % (season.getSubSeasonDuration() / season.getDayDuration()), 0, 8);
        }

        return temp -> temp;
    }
}
