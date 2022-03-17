package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.List;

public class BiomeTempModifier extends TempModifier
{
    public BiomeTempModifier()
    {
    }

    @Override
    public Temperature getResult(Temperature temp, Player player)
    {
        Method getTemperature = ObfuscationReflectionHelper.findMethod(Biome.class, "m_47528_", BlockPos.class);
        getTemperature.setAccessible(true);

        BiomeManager biomeManager = player.level.getBiomeManager();

        try
        {
            double worldTemp = 0;
            for (BlockPos blockPos : WorldHelper.getNearbyPositions(player.blockPosition(), 200, 6))
            {
                Biome biome = biomeManager.getNoiseBiomeAtPosition(blockPos);
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
            return temp.add(worldTemp / 200);
        }
        catch (Exception e) {}

        return temp;
    }

    protected double getTemperatureOffset(ResourceLocation biomeID, ResourceLocation dimensionID)
    {
        double offset = 0;
        for (List<Object> value : ConfigCache.getInstance().worldOptionsReference.get("biome_offsets"))
        {
            if (value.get(0).equals(biomeID.toString()))
            {
                offset += ((Number) value.get(1)).doubleValue();
                break;
            }
        }

        for (List<Object> value : ConfigCache.getInstance().worldOptionsReference.get("dimension_offsets"))
        {
            if (value.get(0).equals(dimensionID.toString()))
            {
                offset += ((Number) value.get(1)).doubleValue();
                break;
            }
        }
        return offset;
    }

    protected TempOverride biomeOverride(ResourceLocation biomeID)
    {
        for (List<?> value : ConfigCache.getInstance().worldOptionsReference.get("biome_temperatures"))
        {
            if (value.get(0).equals(biomeID.toString()))
                return new TempOverride(true, ((Number) value.get(1)).doubleValue());
        }
        return new TempOverride(false, 0.0d);
    }

    protected TempOverride dimensionOverride(ResourceLocation biomeID)
    {
        for (List<?> value : ConfigCache.getInstance().worldOptionsReference.get("dimension_temperatures"))
        {
            if (value.get(0).equals(biomeID.toString()))
                return new TempOverride(true, ((Number) value.get(1)).doubleValue());
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