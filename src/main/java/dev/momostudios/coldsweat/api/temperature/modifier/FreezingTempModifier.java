package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class FreezingTempModifier extends TempModifier
{
    public FreezingTempModifier(double chill)
    {
        this.addArgument("chill", chill);
    }

    public FreezingTempModifier()
    {
        this(0);
    }

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        return temp -> temp.add(-this.<Double>getArgument("chill"));
    }

    @Override
    public String getID() {
        return "cold_sweat:freezing";
    }
}
