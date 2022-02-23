package dev.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import dev.momostudios.coldsweat.common.temperature.Temperature;

public class WaterskinTempModifier extends TempModifier
{
    public WaterskinTempModifier() {}

    public WaterskinTempModifier(double temp)
    {
        addArgument("temperature", temp);
    }

    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        return temp.get() + (double) getArgument("temperature");
    }

    public String getID()
    {
        return "cold_sweat:waterskin";
    }
}