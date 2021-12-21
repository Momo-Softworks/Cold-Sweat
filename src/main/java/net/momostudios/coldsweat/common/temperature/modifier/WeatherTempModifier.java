package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;

public class WeatherTempModifier extends TempModifier
{
    public WeatherTempModifier()
    {
        addArgument("weatherTemp", 0.0);
    }

    @Override
    public double getValue(Temperature temp, PlayerEntity player)
    {
        if (!player.world.canBlockSeeSky(player.getPosition()))
            return temp.get();

        if (player.ticksExisted % 10 > 0)
        {
            return temp.get() + (double) getArgument("weatherTemp");
        }

        if (player.world.isRaining() && player.world.getBiome(player.getPosition()).getTemperature(player.getPosition()) < 0.95)
        {
            double weatherTemp = player.world.getBiome(player.getPosition()).getTemperature(player.getPosition()) < 0.15 ? -0.25 : -0.15;
            setArgument("weatherTemp", weatherTemp);
            return temp.get() + weatherTemp;
        }
        else
        {
            setArgument("weatherTemp", 0.0);
        }
        //System.out.println(getArgument("weatherTemp"));

        return temp.get();
    }

    public String getID()
    {
        return "cold_sweat:weather";
    }
}