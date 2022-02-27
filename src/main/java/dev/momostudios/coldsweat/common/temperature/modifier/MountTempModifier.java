package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

public class MountTempModifier extends TempModifier
{
    public MountTempModifier() {
        this(1.0);
    }

    public MountTempModifier(double strength) {
        addArgument("strength", strength);
    }

    @Override
    public double getResult(Temperature temp, Player player)
    {
        return temp.get() * (1.0D - getArgument("strength", Double.class));
    }

    public String getID()
    {
        return "cold_sweat:insulated_mount";
    }
}