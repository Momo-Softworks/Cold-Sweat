package dev.momostudios.coldsweat.common.temperature.modifier.compat;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.minecraft.world.entity.player.Player;
import sereneseasons.api.season.SeasonHelper;

public class SereneSeasonsTempModifier extends TempModifier
{
    @Override
    public double getResult(Temperature temp, Player player)
    {
        double t = temp.get();
        if (player.level.dimensionType().natural())
            switch (SeasonHelper.getSeasonState(player.level).getSubSeason())
            {
                case EARLY_AUTUMN: return t + 0.2f;
                case MID_AUTUMN: return t;
                case LATE_AUTUMN: return t - 0.2f;

                case EARLY_WINTER: return t - 0.4f;
                case MID_WINTER: return t - 0.6f;
                case LATE_WINTER: return t - 0.4f;

                case EARLY_SPRING: return t - 0.2f;
                case MID_SPRING: return t;
                case LATE_SPRING: return t + 0.2f;

                case EARLY_SUMMER: return t + 0.4f;
                case MID_SUMMER: return t + 0.6f;
                case LATE_SUMMER: return t + 0.4f;

                default: return t;
            }
        else
            return t;
    }

    @Override
    public String getID() {
        return "sereneseasons:season";
    }
}
