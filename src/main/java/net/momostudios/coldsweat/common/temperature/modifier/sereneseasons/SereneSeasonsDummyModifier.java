package net.momostudios.coldsweat.common.temperature.modifier.sereneseasons;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;

public class SereneSeasonsDummyModifier extends TempModifier
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        return temp.get();
    }

    @Override
    public String getID() {
        return "sereneseasons:season";
    }
}
