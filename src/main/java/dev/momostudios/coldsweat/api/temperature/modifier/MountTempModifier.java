package dev.momostudios.coldsweat.api.temperature.modifier;

import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class MountTempModifier extends TempModifier
{
    public MountTempModifier() {
        this(1.0);
    }

    public MountTempModifier(double strength) {
        this.getNBT().putDouble("strength", strength);
    }

    @Override
    public Function<Double, Double> calculate(Player player)
    {
        return temp -> temp * (1.0D - Math.min(1, this.getNBT().getDouble("strength")));
    }

    public String getID()
    {
        return "cold_sweat:insulated_mount";
    }
}