package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.config.WorldTemperatureConfig;
import net.momostudios.coldsweat.core.util.WorldInfo;

import java.util.List;

public class BiomeTempModifier extends TempModifier
{
    public BiomeTempModifier()
    {
        addArgument("value", 0d);
    }

    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        if (player.world.getGameTime() % 5 == 0)
        {
            double worldTemp = 0;
            for (BlockPos blockPos : WorldInfo.getNearbyPositions(player.getPosition(), 200, 6))
            {
                Biome biome = player.world.getBiome(blockPos);
                worldTemp += biome.getTemperature(blockPos) + getTemperatureOffset(biome.getRegistryName(), player.world.getDimensionKey().getLocation());

                // Should temperature be overridden by config
                TempOverride biomeOverride = biomeOverride(biome.getRegistryName());
                TempOverride dimensionOverride = dimensionOverride(player.world.getDimensionKey().getLocation());

                if (dimensionOverride.override)
                {
                    setArgument("value", dimensionOverride.value);
                    return dimensionOverride.value;
                }
                if (biomeOverride.override)
                {
                    setArgument("value", biomeOverride.value);
                    return biomeOverride.value;
                }
            }
            setArgument("value", temp.get() + (worldTemp / 200));
        }
        return (double) getArgument("value");
    }

    protected double getTemperatureOffset(ResourceLocation biomeID, ResourceLocation dimensionID)
    {
        double offset = 0;
        for (List<String> value : ConfigCache.getInstance().worldOptionsReference.get("biome_offsets"))
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                offset += Double.parseDouble(value.get(1));
        }

        for (List<String> value : ConfigCache.getInstance().worldOptionsReference.get("dimension_offsets"))
        {
            if (new ResourceLocation(value.get(0)).equals(dimensionID))
                offset += Double.parseDouble(value.get(1));
        }
        return offset;
    }

    protected TempOverride biomeOverride(ResourceLocation biomeID)
    {
        for (List<String> value : ConfigCache.getInstance().worldOptionsReference.get("biome_temperatures"))
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                return new TempOverride(true, Double.parseDouble(value.get(1)));
        }
        return new TempOverride(false, 0.0d);
    }

    protected TempOverride dimensionOverride(ResourceLocation biomeID)
    {
        for (List<String> value : ConfigCache.getInstance().worldOptionsReference.get("dimension_temperatures"))
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                return new TempOverride(true, Double.parseDouble(value.get(1)));
        }
        return new TempOverride(false, 0.0d);
    }

    private static class TempOverride
    {
        public boolean override;
        public double value;

        public TempOverride(boolean override, double value)
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