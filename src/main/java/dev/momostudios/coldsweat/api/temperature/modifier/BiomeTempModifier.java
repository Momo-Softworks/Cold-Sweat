package dev.momostudios.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.LegacyMethodHelper;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.function.Function;

public class BiomeTempModifier extends TempModifier
{
    static int SAMPLES = 64;

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        try
        {
            double worldTemp = 0;
            ResourceLocation dimensionID = player.level.dimension().location();
            Number dimensionOverride = ConfigSettings.DIMENSION_TEMPS.get().get(dimensionID);

            if (dimensionOverride != null)
            {
                return temp -> temp.add(dimensionOverride.doubleValue());
            }
            else
            {
                double time = Math.sin(player.level.getDayTime() / (12000 / Math.PI));

                for (BlockPos blockPos : WorldHelper.getNearbyPositions(player.blockPosition(), SAMPLES, 16))
                {
                    Biome biome = LegacyMethodHelper.getBiome(player.level, blockPos);
                    if (biome == null) continue;
                    ResourceLocation biomeID = biome.getRegistryName();

                    Pair<Double, Double> configTemp;
                    double biomeVariance = 1 / Math.max(1, 2 + biome.getDownfall() * 2);
                    double biomeTemp = biome.getBaseTemperature();

                    // Get the biome's temperature, either overridden by config or calculated
                    // Start with biome override
                    configTemp = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeID,
                                 // If no override, check for offset
                                 Pair.of((configTemp = ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeID,
                                 // If no offset, do nothing
                                 Pair.of(0d, 0d)))
                                 // Add the biome's base temperature and calculate min/max based on biome's humidity
                                 .getFirst() + biomeTemp - biomeVariance, configTemp.getSecond() + biomeTemp + biomeVariance));

                    // Biome temp at midnight (bottom of the sine wave)
                    double min = configTemp.getFirst();
                    // Biome temp at noon (top of the sine wave)
                    double max = configTemp.getSecond();

                    // If time doesn't exist in the player's dimension, don't use it
                    DimensionType dimension = player.level.dimensionType();
                    if (!dimension.hasCeiling())
                        worldTemp += CSMath.blend(min, max, time, -1, 1) / SAMPLES;
                    else
                        worldTemp += CSMath.average(max, min) / SAMPLES;
                }

                worldTemp += ConfigSettings.DIMENSION_OFFSETS.get().getOrDefault(dimensionID, 0d);
            }
            double finalWorldTemp = worldTemp;
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