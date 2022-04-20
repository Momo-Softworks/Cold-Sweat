package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

public class HellLampTempModifier extends TempModifier
{
    @Override
    public Temperature getResult(Temperature temp, Player player)
    {
        return temp.add(0.8);
    }

    @Override
    public String getID() {
        return "cold_sweat:hellspring_lamp";
    }
}
