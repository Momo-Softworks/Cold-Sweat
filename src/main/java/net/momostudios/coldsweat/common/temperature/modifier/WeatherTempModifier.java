package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;

public class WeatherTempModifier extends TempModifier
{
    @Override
    public double getValue(Temperature temp, PlayerEntity player)
    {
        float weatherTemp = 0;
        if (!player.world.canBlockSeeSky(player.getPosition()))
            return temp.get();

        if (player.world.isRaining() && player.world.getBiome(player.getPosition()).getTemperature(player.getPosition()) < 0.95)
        {
            if (player.world.getBiome(player.getPosition()).getTemperature(player.getPosition()) < 0.15)
                weatherTemp = -0.25f;
            else
                weatherTemp = -0.15f;
        }

        return temp.get() + weatherTemp;
    }

    public String getID()
    {
        return "cold_sweat:weather";
    }
}