package dev.momostudios.coldsweat.api.temperature.modifier.compat;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import net.minecraft.world.entity.player.Player;
import sereneseasons.api.season.SeasonHelper;

public class SereneSeasonsTempModifier extends TempModifier
{
    @Override
    public Temperature getResult(Temperature temp, Player player)
    {
        double t = temp.get();
        if (player.level.dimensionType().natural())
        {
            switch (SeasonHelper.getSeasonState(player.level).getSubSeason())
            {
                case EARLY_AUTUMN: return new Temperature(t + 0.2f);
                case MID_AUTUMN:   return new Temperature(t);
                case LATE_AUTUMN:  return new Temperature(t - 0.2f);

                case EARLY_WINTER: return new Temperature(t - 0.4f);
                case MID_WINTER:   return new Temperature(t - 0.6f);
                case LATE_WINTER:  return new Temperature(t - 0.4f);

                case EARLY_SPRING: return new Temperature(t - 0.2f);
                case MID_SPRING:   return new Temperature(t);
                case LATE_SPRING:  return new Temperature(t + 0.2f);

                case EARLY_SUMMER: return new Temperature(t + 0.4f);
                case MID_SUMMER:   return new Temperature(t + 0.6f);
                case LATE_SUMMER:  return new Temperature(t + 0.4f);

                default:
                    new Temperature(t);
            }
        }

        return new Temperature(t);
    }

    @Override
    public String getID() {
        return "sereneseasons:season";
    }
}
