package net.momostudios.coldsweat.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.temperature.Temperature;
import net.momostudios.coldsweat.util.world.WorldInfo;

public class TimeTempModifier extends TempModifier
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        double worldTemp = WorldInfo.getBiomeTemperature(player.getPosition(), player.world, 200, 6);

        return temp.get() + worldTemp;
    }
}