package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

public class WaterskinTempModifier extends TempModifier
{
    public WaterskinTempModifier()
    {
        addArgument("temperature", 0.0d);
    }

    public WaterskinTempModifier(double temp)
    {
        addArgument("temperature", temp);
    }

    @Override
    public Temperature getResult(Temperature temp, Player player)
    {
        return temp.add(this.<Double>getArgument("temperature"));
    }

    public String getID()
    {
        return "cold_sweat:waterskin";
    }
}