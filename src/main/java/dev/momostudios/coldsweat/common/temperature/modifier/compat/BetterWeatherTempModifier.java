/*package dev.momostudios.coldsweat.common.temperature.modifier.compat;

import corgitaco.betterweather.api.season.Season;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.minecraft.world.entity.player.Player;

public class BetterWeatherTempModifier extends TempModifier
{
    @Override
    public double getResult(Temperature temp, Player player)
    {
        double t = temp.get();
        Season season = Season.getSeason(player.level);
        if (season != null && player.level.getDimensionType().isNatural())
        {
            switch (season.getKey())
            {
                case AUTUMN:
                    switch (season.getPhase())
                    {
                        case START: return t + 0.2f;
                        case MID:   return t;
                        case END:   return t - 0.2f;
                    }

                case WINTER:
                    switch (season.getPhase())
                    {
                        case START: return t - 0.4f;
                        case MID:   return t - 0.6f;
                        case END:   return t - 0.4f;
                    }

                case SPRING:
                    switch (season.getPhase())
                    {
                        case START: return t - 0.2f;
                        case MID:   return t;
                        case END:   return t + 0.2f;
                    }

                case SUMMER:
                    switch (season.getPhase())
                    {
                        case START: return t + 0.4f;
                        case MID:   return t + 0.6f;
                        case END:   return t + 0.4f;
                    }

                default:
                    return t;
            }
        }
        else
            return t;
    }

    @Override
    public String getID() {
        return "betterweather:season";
    }
}*/
