package dev.momostudios.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.serialization.Triplet;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.function.Function;

public class BiomeTempModifier extends TempModifier
{
    public BiomeTempModifier()
    {
        this(16);
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
            World world = entity.level;
            double worldTemp = 0;
            ResourceLocation dimensionID = world.dimension().location();

            worldTemp += ConfigSettings.DIMENSION_OFFSETS.get().getOrDefault(dimensionID, 0d);
            Number dimensionOverride = ConfigSettings.DIMENSION_TEMPS.get().get(dimensionID);

            if (dimensionOverride != null)
            {   return temp -> temp + dimensionOverride.doubleValue();
            }
            else
            {
                // Fallback for improper sample count
                int samples;
                if (this.getNBT().contains("Samples"))
                {   samples = this.getNBT().getInt("Samples");
                }
                else
                {   samples = 25;
                    this.getNBT().putInt("Samples", 25);
                }


                for (BlockPos blockPos : WorldHelper.getPositionGrid(entity.blockPosition(), samples, 16))
                {
                    int x = blockPos.getX();
                    int y = blockPos.getY();
                    int z = blockPos.getZ();
                    Biome biome = world.getUncachedNoiseBiome(x >> 2, y >> 2, z >> 2);
                    ResourceLocation biomeID = biome.getRegistryName();

                    Pair<Double, Double> configTemp;
                    double biomeVariance = 1 / Math.max(1, 2 + biome.getDownfall() * 2);
                    double baseTemp = biome.getBaseTemperature();

                    // Get the biome's temperature, either overridden by config or calculated
                    // Start with biome override
                    Triplet<Double, Double, Temperature.Units> cTemp = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeID,
                                                                       new Triplet<>(baseTemp - biomeVariance, baseTemp + biomeVariance, Temperature.Units.MC));
                    Triplet<Double, Double, Temperature.Units> cOffset = ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeID,
                                                                         new Triplet<>(0d, 0d, Temperature.Units.MC));
                    configTemp = CSMath.addPairs(Pair.of(cTemp.getFirst(), cTemp.getSecond()), Pair.of(cOffset.getFirst(), cOffset.getSecond()));

                    // Biome temp at midnight (bottom of the sine wave)
                    double min = configTemp.getFirst();
                    // Biome temp at noon (top of the sine wave)
                    double max = configTemp.getSecond();

                    // Divide by this to get average

                    DimensionType dimension = world.dimensionType();
                    if (!dimension.hasCeiling())
                    {
                        double altitude = entity.getY();
                        double mid = (min + max) / 2;
                        // Biome temp with time of day
                        worldTemp += CSMath.blend(min, max, Math.sin(world.getDayTime() / (12000 / Math.PI)), -1d, 1d) / samples
                                   // Altitude calculation
                                   + CSMath.blend(0, Math.min(-0.6, (min - mid) * 2), altitude, world.getSeaLevel(), world.getMaxBuildHeight()) / samples;
                    }
                    // If dimension has ceiling (don't use time or altitude)
                    else worldTemp += CSMath.average(max, min) / samples;
                }
            }
            double finalWorldTemp = worldTemp;
            return temp -> temp + finalWorldTemp;
        }
        catch (Exception e)
        {   return temp -> temp;
        }
    }

    public String getID()
    {
        return "cold_sweat:biome";
    }
}