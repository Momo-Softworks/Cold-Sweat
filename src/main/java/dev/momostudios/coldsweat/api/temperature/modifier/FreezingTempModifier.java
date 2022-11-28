package dev.momostudios.coldsweat.api.temperature.modifier;

import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class FreezingTempModifier extends TempModifier
{
    public FreezingTempModifier(double chill)
    {
        this.getNBT().putDouble("chill", chill);
    }

    public FreezingTempModifier()
    {
        this(0);
    }

    @Override
    public Function<Double, Double> calculate(Player player)
    {
        return temp -> temp - this.getNBT().getDouble("chill");
    }

    @Override
    public String getID() {
        return "cold_sweat:freezing";
    }
}
