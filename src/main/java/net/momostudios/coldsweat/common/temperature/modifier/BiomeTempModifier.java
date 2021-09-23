package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.config.WorldTemperatureConfig;
import net.momostudios.coldsweat.core.util.WorldInfo;
import net.momostudios.coldsweat.common.temperature.Temperature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BiomeTempModifier extends TempModifier implements IForgeRegistryEntry<TempModifier>
{
    WorldTemperatureConfig config = WorldTemperatureConfig.getInstance();

    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        double worldTemp = 0;
        for (BlockPos iterator : WorldInfo.getNearbyPositions(player.getPosition(), 200, 6))
        {
            Biome biome = player.world.getBiome(iterator);
            worldTemp += biome.getTemperature(iterator) + getTemperatureOffset(biome.getRegistryName(), player.world.getDimensionKey().getLocation());

            // Should temperature be overridden by config
            if ((boolean) dimensionOverride(player.world.getDimensionKey().getLocation()).get(0))
            {
                return (double) dimensionOverride(player.world.getDimensionKey().getLocation()).get(1);
            }
            if ((boolean) biomeOverride(biome.getRegistryName()).get(0))
            {
                return (double) biomeOverride(biome.getRegistryName()).get(1);
            }
        }
        return temp.get() + (worldTemp / 200);
    }

    protected double getTemperatureOffset(ResourceLocation biomeID, ResourceLocation dimensionID)
    {
        double offset = 0;
        for (List<String> value : config.biomeOffsets())
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                offset += Double.parseDouble(value.get(1));
        }

        for (List<String> value : config.dimensionOffsets())
        {
            if (new ResourceLocation(value.get(0)).equals(dimensionID))
                offset += Double.parseDouble(value.get(1));
        }
        return offset;
    }

    protected List biomeOverride(ResourceLocation biomeID)
    {
        for (List<String> value : config.biomeTemperatures())
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                return Arrays.asList(true, Double.parseDouble(value.get(1)));
        }
        return Arrays.asList(false, 0.0d);
    }

    protected List dimensionOverride(ResourceLocation biomeID)
    {
        for (List<String> value : config.dimensionTemperatures())
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                return Arrays.asList(true, Double.parseDouble(value.get(1)));
        }
        return Arrays.asList(false, 0.0d);
    }

    public String getID()
    {
        return "cold_sweat:biome_temperature";
    }
}