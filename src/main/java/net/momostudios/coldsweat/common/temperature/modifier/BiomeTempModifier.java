package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.config.WorldTemperatureConfig;
import net.momostudios.coldsweat.core.util.WorldInfo;

import java.util.Arrays;
import java.util.List;

public class BiomeTempModifier extends TempModifier
{
    public BiomeTempModifier()
    {
        addArgument("value", 0d);
    }

    @java.lang.Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        if (player.ticksExisted % 5 == 0)
        {
            double worldTemp = 0;
            for (BlockPos blockPos : WorldInfo.getNearbyPositions(player.getPosition(), 200, 6))
            {
                Biome biome = player.world.getBiome(blockPos);
                worldTemp += biome.getTemperature(blockPos) + getTemperatureOffset(biome.getRegistryName(), player.world.getDimensionKey().getLocation(), player);

                // Should temperature be overridden by config
                Override biomeOverride = biomeOverride(biome.getRegistryName(), player);
                Override dimensionOverride = dimensionOverride(player.world.getDimensionKey().getLocation(), player);

                if (dimensionOverride.override) return dimensionOverride.value;
                if (biomeOverride.override) return biomeOverride.value;
            }
            setArgument("value", temp.get() + (worldTemp / 200));
        }
        return (double) getArgument("value");
    }

    protected double getTemperatureOffset(ResourceLocation biomeID, ResourceLocation dimensionID, PlayerEntity player)
    {
        double offset = 0;
        for (List<String> value : (player instanceof ServerPlayerEntity ? WorldTemperatureConfig.INSTANCE.biomeOffsets() :
                ConfigCache.getInstance().worldOptionsReference.get("biome_offsets")))
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                offset += Double.parseDouble(value.get(1));
        }

        for (List<String> value : (player instanceof ServerPlayerEntity ? WorldTemperatureConfig.INSTANCE.dimensionOffsets() :
                ConfigCache.getInstance().worldOptionsReference.get("dimension_offsets")))
        {
            if (new ResourceLocation(value.get(0)).equals(dimensionID))
                offset += Double.parseDouble(value.get(1));
        }
        return offset;
    }

    protected Override biomeOverride(ResourceLocation biomeID, PlayerEntity player)
    {
        for (List<String> value : (player instanceof ServerPlayerEntity ? WorldTemperatureConfig.INSTANCE.biomeTemperatures() :
                ConfigCache.getInstance().worldOptionsReference.get("biome_temperatures")))
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                return new Override(true, Double.parseDouble(value.get(1)));
        }
        return new Override(false, 0.0d);
    }

    protected Override dimensionOverride(ResourceLocation biomeID, PlayerEntity player)
    {
        for (List<String> value : (player instanceof ServerPlayerEntity ? WorldTemperatureConfig.INSTANCE.dimensionTemperatures() :
                ConfigCache.getInstance().worldOptionsReference.get("biome_temperatures")))
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