package dev.momostudios.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

public class BiomeTempModifier extends TempModifier
{
    public BiomeTempModifier()
    {
        this(25);
    }

    public BiomeTempModifier(int samples)
    {
        this.getNBT().putInt("Samples", samples);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        try
        {
            double worldTemp = 0;
            ResourceLocation dimensionID = entity.level.dimension().location();
            Number dimensionOverride = ConfigSettings.DIMENSION_TEMPS.get().get(dimensionID);

            if (dimensionOverride != null)
            {
                return temp -> temp + dimensionOverride.doubleValue();
            }
            else
            {
                int samples = this.getNBT().getInt("Samples");
                // Failsafe for old TempModifiers
                if (samples < 1)
                {   samples = 25;
                    this.getNBT().putInt("Samples", 25);
                }

                for (BlockPos blockPos : WorldHelper.getPositionGrid(entity.blockPosition(), samples, 16))
                {
                    Biome biome = entity.level.getBiomeManager().getBiome(blockPos).value();
                    ResourceLocation biomeID = ForgeRegistries.BIOMES.getKey(biome);

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
                    DimensionType dimension = entity.level.dimensionType();
                    if (!dimension.hasCeiling())
                        worldTemp += CSMath.blend(min, max, Math.sin(entity.level.getDayTime() / (12000 / Math.PI)), -1, 1) / samples;
                    else
                        worldTemp += CSMath.average(max, min) / samples;
                }

                worldTemp += ConfigSettings.DIMENSION_OFFSETS.get().getOrDefault(dimensionID, 0d);
            }
            double finalWorldTemp = worldTemp;
            return temp -> temp + finalWorldTemp;
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