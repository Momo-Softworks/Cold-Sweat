package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class InsulationTempModifier extends TempModifier
{
    public InsulationTempModifier()
    {
        addArgument("warmth", 0);
    }

    public InsulationTempModifier(int amount)
    {
        addArgument("warmth", amount);
    }

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        double warmth = Math.max(1d, this.<Integer>getArgument("warmth") / 10d);
        return temp -> temp.divide(warmth);
    }

    public String getID()
    {
        return "cold_sweat:insulated_armor";
    }
}