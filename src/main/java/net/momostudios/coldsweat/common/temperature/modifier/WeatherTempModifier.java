package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;

public class WeatherTempModifier extends TempModifier implements IForgeRegistryEntry<TempModifier>
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        double weatherTemp = 0;
        if (player.world.canBlockSeeSky(player.getPosition()))
        {
            if (player.world.isRaining() && player.world.getBiome(player.getPosition()).getTemperature(player.getPosition()) < 0.95)
            {
                if (player.world.getBiome(player.getPosition()).getTemperature(player.getPosition()) < 0.15)
                    weatherTemp = -0.25;
                else
                    weatherTemp = -0.15;
            }
        }

        return temp.get() + weatherTemp;
    }

    public String getID()
    {
        return "cold_sweat:weather";
    }
}