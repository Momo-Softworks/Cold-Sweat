package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class InsulationTempModifier extends TempModifier
{
    public InsulationTempModifier()
    {
        this(0d, 0d);
    }

    public InsulationTempModifier(double cold, double hot)
    {
        addArgument("cold", cold);
        addArgument("hot", hot);
    }

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        double cold = Math.max(1d, this.<Double>getArgument("cold") / 8d);
        double hot = Math.max(1d, this.<Double>getArgument("hot") / 8d);
        return temp -> temp.divide(temp.get() > 0 ? hot : cold);
    }

    public String getID()
    {
        return "cold_sweat:insulated_armor";
    }
}