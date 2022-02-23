package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.entity.player.PlayerEntity;

public class FoodTempModifier extends TempModifier
{
    public FoodTempModifier()
    {
        this(0);
    }

    public FoodTempModifier(double effect)
    {
        addArgument("effect", effect);
    }

    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        return temp.get() + getArgument("effect", Double.class);
    }

    @Override
    public String getID()
    {
        return "cold_sweat:food";
    }
}
