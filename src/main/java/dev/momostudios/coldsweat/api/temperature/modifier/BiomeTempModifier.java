package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.WorldSettingsConfig;
import dev.momostudios.coldsweat.util.LegacyMethodHelper;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.config.DynamicValue;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BiomeTempModifier extends TempModifier
{
    static DynamicValue<Map<ResourceLocation, Number>> BIOME_TEMPS = DynamicValue.of(() ->
            ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().biomeTemperatures()));

    static DynamicValue<Map<ResourceLocation, Number>> BIOME_OFFSETS = DynamicValue.of(() ->
            ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().biomeOffsets()));

    static DynamicValue<Map<ResourceLocation, Number>> DIMENSION_TEMPS = DynamicValue.of(() ->
    {
        Map<ResourceLocation, Number> map = new HashMap<>();
        for (List<?> entry : WorldSettingsConfig.getInstance().dimensionTemperatures())
        {
            map.put(new ResourceLocation((String) entry.get(0)), (Number) entry.get(1));
        }
        return map;
    });

    static DynamicValue<Map<ResourceLocation, Number>> DIMENSION_OFFSETS = DynamicValue.of(() ->
    {
        Map<ResourceLocation, Number> map = new HashMap<>();
        for (List<?> entry : WorldSettingsConfig.getInstance().dimensionOffsets())
        {
            map.put(new ResourceLocation((String) entry.get(0)), (Number) entry.get(1));
        }
        return map;
    });

    static int SAMPLES = 64;

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        try
        {
            double worldTemp = 0;
            for (BlockPos blockPos : WorldHelper.getNearbyPositions(player.blockPosition(), SAMPLES, 16))
            {
                Biome biome = LegacyMethodHelper.getBiome(player.level, blockPos);
                if (biome == null) continue;
                ResourceLocation biomeID = biome.getRegistryName();
                ResourceLocation dimensionID = player.level.dimension().location();

                // Should temperature be overridden by config
                Number biomeOverride = BIOME_TEMPS.get().get(biomeID);
                Number dimensionOverride = DIMENSION_TEMPS.get().get(dimensionID);

                if (dimensionOverride != null)
                {
                    worldTemp += dimensionOverride.doubleValue();
                    continue;
                }
                if (biomeOverride != null)
                {
                    worldTemp += biomeOverride.doubleValue();
                    continue;
                }

                // If temperature is not overridden, get biome temp & apply the offsets
                worldTemp += biome.getBaseTemperature()
                           + BIOME_OFFSETS.get().getOrDefault(biomeID, 0).doubleValue()
                           + DIMENSION_OFFSETS.get().getOrDefault(dimensionID, 0).doubleValue();

            }
            double finalWorldTemp = worldTemp / SAMPLES;
            return temp -> temp.add(finalWorldTemp);
        }
        catch (Exception e)
        {
            return (temp) -> temp;
        }
    }

    public String getID()
    {
        return "cold_sweat:biome_temperature";
    }
}