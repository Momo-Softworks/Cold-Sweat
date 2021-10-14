package net.momostudios.coldsweat.common.temperature.modifier.sereneseasons;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import sereneseasons.api.season.SeasonHelper;

public class SereneSeasonsTempModifier extends TempModifier
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        double t = temp.get();
        switch (SeasonHelper.getSeasonState(player.world).getSubSeason())
        {
            case EARLY_AUTUMN: return t + 0.2;
            case MID_AUTUMN: return t;
            case LATE_AUTUMN: return t - 0.2;

            case EARLY_WINTER: return t - 0.4;
            case MID_WINTER: return t - 0.6;
            case LATE_WINTER: return t - 0.4;

            case EARLY_SPRING: return t - 0.2;
            case MID_SPRING: return t;
            case LATE_SPRING: return t + 0.2;

            case EARLY_SUMMER: return t + 0.4;
            case MID_SUMMER: return t + 0.6;
            case LATE_SUMMER: return t + 0.4;

            default: return t;
        }
    }

    @Override
    public String getID() {
        return "sereneseasons:season";
    }
}
