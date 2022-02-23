package dev.momostudios.coldsweat.common.temperature.modifier.compat;

import net.minecraft.entity.player.PlayerEntity;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import sereneseasons.api.season.SeasonHelper;

public class SereneSeasonsTempModifier extends TempModifier
{
    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        double t = temp.get();
        if (player.world.getDimensionType().isNatural())
            switch (SeasonHelper.getSeasonState(player.world).getSubSeason())
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
