package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.util.LegacyMappings;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class BiomeTempModifier extends TempModifier
{
    static Method getTemperature = ObfuscationReflectionHelper.findMethod(Biome.class, "m_47528_", BlockPos.class);
    static
    {
        getTemperature.setAccessible(true);
    }

    static Map<ResourceLocation, Double> biomeOverrides = new HashMap<>();
    static Map<ResourceLocation, Double> biomeOffsets = new HashMap<>();
    static Map<ResourceLocation, Double> dimensionOverrides = new HashMap<>();
    static Map<ResourceLocation, Double> dimensionOffsets = new HashMap<>();
    @Override
    public Temperature getResult(Temperature temp, Player player)
    {
        BiomeManager biomeManager = player.level.getBiomeManager();

        try
        {
            double worldTemp = 0;
            for (BlockPos blockPos : WorldHelper.getNearbyPositions(player.blockPosition(), 100, 10))
            {
                Biome biome;

                // For 1.18.1
                if (FMLLoader.versionInfo().mcVersion().equals("1.18.1"))
                {
                    biome = (Biome) LegacyMappings.getBiomeOld.invoke(biomeManager, blockPos);
                }
                // For 1.18.2
                else
                {
                    biome = biomeManager.getBiome(blockPos).value();
                }

                worldTemp += (float) getTemperature.invoke(biome, blockPos) + getTemperatureOffset(biome.getRegistryName(), player.level.dimension().location());

                // Should temperature be overridden by config
                TempOverride biomeOverride = biomeOverride(biome.getRegistryName());
                TempOverride dimensionOverride = dimensionOverride(player.level.dimension().location());

                if (dimensionOverride.override)
                {
                    return new Temperature(dimensionOverride.value);
                }
                if (biomeOverride.override)
                {
                    return new Temperature(biomeOverride.value);
                }
            }
            return temp.add(worldTemp / 100);
        }
        catch (Exception e)
        {
            return temp;
        }
    }

    protected double getTemperatureOffset(ResourceLocation biomeID, ResourceLocation dimensionID)
    {
        double offset = 0;
        if (biomeOffsets.containsKey(biomeID))
        {
            offset += biomeOffsets.get(biomeID);
        }
        else
        {
            double foundValue;
            for (List<Object> value : ConfigCache.getInstance().worldOptionsReference.get("biome_offsets"))
            {
                if (value.get(0).equals(biomeID.toString()))
                {
                    foundValue = ((Number) value.get(1)).doubleValue();
                    offset += foundValue;
                    biomeOffsets.put(biomeID, foundValue);
                    break;
                }
            }
            biomeOffsets.put(biomeID, offset);
        }

        if (dimensionOffsets.containsKey(biomeID))
        {
            offset += dimensionOffsets.get(biomeID);
        }
        else
        {
            double foundValue;
            for (List<Object> value : ConfigCache.getInstance().worldOptionsReference.get("dimension_offsets"))
            {
                if (value.get(0).equals(dimensionID.toString()))
                {
                    foundValue = ((Number) value.get(1)).doubleValue();
                    offset += foundValue;
                    dimensionOffsets.put(biomeID, foundValue);
                    break;
                }
            }
        }
        return offset;
    }

    protected TempOverride biomeOverride(ResourceLocation biomeID)
    {
        if (biomeOverrides.containsKey(biomeID))
        {
            return new TempOverride(true, biomeOverrides.get(biomeID));
        }

        for (List<?> value : ConfigCache.getInstance().worldOptionsReference.get("biome_temperatures"))
        {
            if (value.get(0).equals(biomeID.toString()))
            {
                double temp = ((Number) value.get(1)).doubleValue();
                biomeOverrides.put(biomeID, temp);
                return new TempOverride(true, temp);
            }
        }
        return new TempOverride(false, 0.0d);
    }

    protected TempOverride dimensionOverride(ResourceLocation dimensionID)
    {
        if (dimensionOverrides.containsKey(dimensionID))
        {
            return new TempOverride(true, dimensionOverrides.get(dimensionID));
        }

        for (List<?> value : ConfigCache.getInstance().worldOptionsReference.get("dimension_temperatures"))
        {
            if (value.get(0).equals(dimensionID.toString()))
            {
                double temp = ((Number) value.get(1)).doubleValue();
                dimensionOverrides.put(dimensionID, temp);
                return new TempOverride(true, temp);
            }
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

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event)
    {
        biomeOffsets.clear();
        biomeOverrides.clear();
        dimensionOffsets.clear();
        dimensionOverrides.clear();
    }

    public String getID()
    {
        return "cold_sweat:biome_temperature";
    }
}