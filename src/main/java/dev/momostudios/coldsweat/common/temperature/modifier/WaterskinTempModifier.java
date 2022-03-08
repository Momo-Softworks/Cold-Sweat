package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

public class WaterskinTempModifier extends TempModifier
{
    public WaterskinTempModifier()
    {
        addArgument("temperature", 0);
    }

    public WaterskinTempModifier(double temp)
    {
        addArgument("temperature", temp);
    }

    @Override
    public double getResult(Temperature temp, Player player)
    {
        return temp.get() + (double) getArgument("temperature");
    }

    public String getID()
    {
        return "cold_sweat:waterskin";
    }
}