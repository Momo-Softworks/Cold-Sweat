package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.WorldTemperatureConfig;
import net.momostudios.coldsweat.core.util.WorldInfo;

import java.util.Arrays;
import java.util.List;

public class BiomeTempModifier extends TempModifier
{
    WorldTemperatureConfig config = new WorldTemperatureConfig();

    @java.lang.Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        double worldTemp = 0;
        for (BlockPos iterator : WorldInfo.getNearbyPositions(player.getPosition(), 200, 6))
        {
            Biome biome = player.world.getBiome(iterator);
            worldTemp += biome.getTemperature(iterator) + getTemperatureOffset(biome.getRegistryName(), player.world.getDimensionKey().getLocation());

            // Should temperature be overridden by config
            if (dimensionOverride(player.world.getDimensionKey().getLocation()).override)
            {
                return dimensionOverride(player.world.getDimensionKey().getLocation()).value;
            }
            if (biomeOverride(biome.getRegistryName()).override)
            {
                return biomeOverride(biome.getRegistryName()).value;
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

    protected Override biomeOverride(ResourceLocation biomeID)
    {
        for (List<String> value : config.biomeTemperatures())
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                return new Override(true, Double.parseDouble(value.get(1)));
        }
        return new Override(false, 0.0d);
    }

    protected Override dimensionOverride(ResourceLocation biomeID)
    {
        for (List<String> value : config.dimensionTemperatures())
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                return new Override(true, Double.parseDouble(value.get(1)));
        }
        return new Override(false, 0.0d);
    }

    private static class Override
    {
        public boolean override;
        public double value;

        public Override(boolean override, double value)
        {
            this.override = override;
            this.value = value;
        }
    }

    public String getID()
    {
        return "cold_sweat:biome_temperature";
    }
}