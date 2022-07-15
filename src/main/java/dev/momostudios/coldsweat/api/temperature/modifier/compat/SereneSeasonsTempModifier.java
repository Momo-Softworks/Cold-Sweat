package dev.momostudios.coldsweat.api.temperature.modifier.compat;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.config.WorldSettingsConfig;
import dev.momostudios.coldsweat.util.config.LoadedValue;
import net.minecraft.world.entity.player.Player;
import sereneseasons.api.season.SeasonHelper;

import java.util.function.Function;

public class SereneSeasonsTempModifier extends TempModifier
{
    static LoadedValue<Double[]> SUMMER_TEMPS = LoadedValue.of(() -> WorldSettingsConfig.getInstance().summerTemps());
    static LoadedValue<Double[]> AUTUMN_TEMPS = LoadedValue.of(() -> WorldSettingsConfig.getInstance().autumnTemps());
    static LoadedValue<Double[]> WINTER_TEMPS = LoadedValue.of(() -> WorldSettingsConfig.getInstance().winterTemps());
    static LoadedValue<Double[]> SPRING_TEMPS = LoadedValue.of(() -> WorldSettingsConfig.getInstance().springTemps());

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        if (player.level.dimensionType().natural())
        {
            double value = switch (SeasonHelper.getSeasonState(player.level).getSubSeason())
            {
                case EARLY_AUTUMN -> AUTUMN_TEMPS.get()[0];
                case MID_AUTUMN   -> AUTUMN_TEMPS.get()[1];
                case LATE_AUTUMN  -> AUTUMN_TEMPS.get()[2];

                case EARLY_WINTER -> WINTER_TEMPS.get()[0];
                case MID_WINTER   -> WINTER_TEMPS.get()[1];
                case LATE_WINTER  -> WINTER_TEMPS.get()[2];

                case EARLY_SPRING -> SPRING_TEMPS.get()[0];
                case MID_SPRING   -> SPRING_TEMPS.get()[1];
                case LATE_SPRING  -> SPRING_TEMPS.get()[2];

                case EARLY_SUMMER -> SUMMER_TEMPS.get()[0];
                case MID_SUMMER   -> SUMMER_TEMPS.get()[1];
                case LATE_SUMMER  -> SUMMER_TEMPS.get()[2];
            };
            return temp -> temp.add(value);
        }

        return temp -> temp;
    }

    @Override
    public String getID() {
        return "sereneseasons:season";
    }
}
