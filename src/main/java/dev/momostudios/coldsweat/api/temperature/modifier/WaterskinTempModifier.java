package dev.momostudios.coldsweat.api.temperature.modifier;

import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class WaterskinTempModifier extends TempModifier
{
    public WaterskinTempModifier()
    {
        this(0.0);
    }

    public WaterskinTempModifier(double temp)
    {
        this.getNBT().putDouble("temperature", temp);
    }

    @Override
    public Function<Double, Double>  calculate(Player player)
    {
        return temp -> temp + this.getNBT().getDouble("temperature");
    }

    public String getID()
    {
        return "cold_sweat:waterskin";
    }
}