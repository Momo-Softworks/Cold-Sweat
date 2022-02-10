package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.util.PlayerHelper;

public class MountTempModifier extends TempModifier
{
    public MountTempModifier() {
        this(1.0);
    }

    public MountTempModifier(double strength) {
        addArgument("strength", strength);
    }

    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        return temp.get() * (1.0D - getArgument("strength", Double.class));
    }

    public String getID()
    {
        return "cold_sweat:insulated_mount";
    }
}