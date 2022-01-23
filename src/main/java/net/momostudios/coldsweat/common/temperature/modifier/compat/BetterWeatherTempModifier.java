package net.momostudios.coldsweat.common.temperature.modifier.compat;

import corgitaco.betterweather.api.season.Season;
import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;

public class BetterWeatherTempModifier extends TempModifier
{
    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        double t = temp.get();
        Season season = Season.getSeason(player.world);
        if (season != null && player.world.getDimensionType().isNatural())
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
}
