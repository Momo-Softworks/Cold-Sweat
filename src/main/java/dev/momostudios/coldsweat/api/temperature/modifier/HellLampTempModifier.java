package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class HellLampTempModifier extends TempModifier
{
    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        return temp -> temp.add(0.8);
    }

    @Override
    public String getID() {
        return "cold_sweat:hellspring_lamp";
    }
}
