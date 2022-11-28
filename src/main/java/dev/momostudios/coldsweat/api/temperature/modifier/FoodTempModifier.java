package dev.momostudios.coldsweat.api.temperature.modifier;

import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class FoodTempModifier extends TempModifier
{
    public FoodTempModifier()
    {
        this(0);
    }

    public FoodTempModifier(double effect)
    {
        this.getNBT().putDouble("effect", effect);
    }

    @Override
    public Function<Double, Double> calculate(Player player)
    {
        return temp -> temp + this.getNBT().getDouble("effect");
    }

    @Override
    public String getID()
    {
        return "cold_sweat:food";
    }
}
