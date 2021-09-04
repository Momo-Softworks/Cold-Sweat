package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.core.util.WorldInfo;

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
}