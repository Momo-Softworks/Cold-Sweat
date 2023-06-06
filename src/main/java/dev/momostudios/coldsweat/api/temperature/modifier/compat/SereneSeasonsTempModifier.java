package dev.momostudios.coldsweat.api.temperature.modifier.compat;

import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;

import java.util.function.Function;

/**
 * Special TempModifier class for Serene Seasons
 */
public class SereneSeasonsTempModifier extends TempModifier
{
    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        if (entity.level.dimensionType().natural())
        {
            ISeasonState season = SeasonHelper.getSeasonState(entity.level);
            double startValue;
            double endValue;
            switch (season.getSubSeason())
            {
                case EARLY_AUTUMN -> { startValue = ConfigSettings.AUTUMN_TEMPS.get()[0]; endValue = ConfigSettings.AUTUMN_TEMPS.get()[1]; }
                case MID_AUTUMN   -> { startValue = ConfigSettings.AUTUMN_TEMPS.get()[1]; endValue = ConfigSettings.AUTUMN_TEMPS.get()[2]; }
                case LATE_AUTUMN  -> { startValue = ConfigSettings.AUTUMN_TEMPS.get()[2]; endValue = ConfigSettings.WINTER_TEMPS.get()[0]; }

                case EARLY_WINTER -> { startValue = ConfigSettings.WINTER_TEMPS.get()[0]; endValue = ConfigSettings.WINTER_TEMPS.get()[1]; }
                case MID_WINTER   -> { startValue = ConfigSettings.WINTER_TEMPS.get()[1]; endValue = ConfigSettings.WINTER_TEMPS.get()[2]; }
                case LATE_WINTER  -> { startValue = ConfigSettings.WINTER_TEMPS.get()[2]; endValue = ConfigSettings.SPRING_TEMPS.get()[0]; }

                case EARLY_SPRING -> { startValue = ConfigSettings.SPRING_TEMPS.get()[0]; endValue = ConfigSettings.SPRING_TEMPS.get()[1]; }
                case MID_SPRING   -> { startValue = ConfigSettings.SPRING_TEMPS.get()[1]; endValue = ConfigSettings.SPRING_TEMPS.get()[2]; }
                case LATE_SPRING  -> { startValue = ConfigSettings.SPRING_TEMPS.get()[2]; endValue = ConfigSettings.SUMMER_TEMPS.get()[0]; }

                case EARLY_SUMMER -> { startValue = ConfigSettings.SUMMER_TEMPS.get()[0]; endValue = ConfigSettings.SUMMER_TEMPS.get()[1]; }
                case MID_SUMMER   -> { startValue = ConfigSettings.SUMMER_TEMPS.get()[1]; endValue = ConfigSettings.SUMMER_TEMPS.get()[2]; }
                case LATE_SUMMER  -> { startValue = ConfigSettings.SUMMER_TEMPS.get()[2]; endValue = ConfigSettings.AUTUMN_TEMPS.get()[0]; }
                default ->
                {
                    return temp -> temp;
                }
            }
            return temp -> temp + (float) CSMath.blend(startValue, endValue, season.getDay() % (season.getSubSeasonDuration() / season.getDayDuration()), 0, 8);
        }

        return temp -> temp;
    }

    @Override
    public String getID() {
        return "sereneseasons:season";
    }
}
