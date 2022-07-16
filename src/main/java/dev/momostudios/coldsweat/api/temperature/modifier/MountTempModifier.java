package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class MountTempModifier extends TempModifier
{
    public MountTempModifier() {
        this(1.0);
    }

    public MountTempModifier(double strength) {
        addArgument("strength", strength);
    }

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        return temp -> temp.multiply(1.0D - this.<Double>getArgument("strength"));
    }

    public String getID()
    {
        return "cold_sweat:insulated_mount";
    }
}