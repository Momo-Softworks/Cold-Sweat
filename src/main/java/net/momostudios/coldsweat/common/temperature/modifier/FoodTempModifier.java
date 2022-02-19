package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;

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
