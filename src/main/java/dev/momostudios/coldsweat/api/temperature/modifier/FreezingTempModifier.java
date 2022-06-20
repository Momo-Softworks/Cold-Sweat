package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

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
    public Temperature getResult(Temperature temp, Player player)
    {
        return temp.add(-this.<Double>getArgument("chill"));
    }

    @Override
    public String getID() {
        return "cold_sweat:freezing";
    }
}
