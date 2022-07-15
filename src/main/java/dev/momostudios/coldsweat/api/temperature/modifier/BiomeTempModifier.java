package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.WorldSettingsConfig;
import dev.momostudios.coldsweat.util.LegacyMethodHelper;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.config.LoadedValue;
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
    static Method GET_TEMPERATURE = ObfuscationReflectionHelper.findMethod(Biome.class, "m_47528_", BlockPos.class);
    static
    {
        GET_TEMPERATURE.setAccessible(true);
    }

    static LoadedValue<Map<ResourceLocation, Number>> BIOME_TEMPS       = LoadedValue.of(() ->
            ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().biomeTemperatures()));

    static LoadedValue<Map<ResourceLocation, Number>> BIOME_OFFSETS     = LoadedValue.of(() ->
            ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().biomeOffsets()));

    static LoadedValue<Map<ResourceLocation, Number>> DIMENSION_TEMPS   = LoadedValue.of(() ->
    {
        Map<ResourceLocation, Number> map = new HashMap<>();
        for (List<?> entry : WorldSettingsConfig.getInstance().dimensionTemperatures())
        {
            map.put(new ResourceLocation((String) entry.get(0)), (Number) entry.get(1));
        }
        return map;
    });

    static LoadedValue<Map<ResourceLocation, Number>> DIMENSION_OFFSETS = LoadedValue.of(() ->
    {
        Map<ResourceLocation, Number> map = new HashMap<>();
        for (List<?> entry : WorldSettingsConfig.getInstance().dimensionOffsets())
        {
            map.put(new ResourceLocation((String) entry.get(0)), (Number) entry.get(1));
        }
        return map;
    });

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        BiomeManager biomeManager = player.level.getBiomeManager();

        try
        {
            double worldTemp = 0;
            for (BlockPos blockPos : WorldHelper.getNearbyPositions(player.blockPosition(), 50, 10))
            {
                Biome biome = LegacyMethodHelper.getBiome(biomeManager, blockPos);
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

                Number biomeOffset = BIOME_OFFSETS.get().get(biomeID);
                Number dimensionOffset = DIMENSION_OFFSETS.get().get(dimensionID);

                // If temperature is not overridden, apply the offsets
                worldTemp += (float) GET_TEMPERATURE.invoke(biome, blockPos);
                if (biomeOffset != null) worldTemp += biomeOffset.doubleValue();
                if (dimensionOffset != null) worldTemp += dimensionOffset.doubleValue();

            }
            double finalWorldTemp = worldTemp;
            return (temp) -> temp.add(finalWorldTemp / 50);
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