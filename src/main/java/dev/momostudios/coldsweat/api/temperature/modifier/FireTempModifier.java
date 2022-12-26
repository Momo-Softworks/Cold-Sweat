package dev.momostudios.coldsweat.api.temperature.modifier;

import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class FireTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(Player player)
    {
        return temp -> player.isOnFire() ? temp + 10 : temp;
    }

    @Override
    public String getID()
    {
        return "cold_sweat:on_fire";
    }
}
