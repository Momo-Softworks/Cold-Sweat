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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            BlockPos entPos = entity.blockPosition();

            // In the case that the dimension temperature is overridden by config, use that and skip everything else
            Pair<Double, Temperature.Units> dimTempOverride = ConfigSettings.DIMENSION_TEMPS.get().get(dimensionID);
            if (dimTempOverride != null)
            {   return temp -> temp + Temperature.convertUnits(dimTempOverride.getFirst(), dimTempOverride.getSecond(), Temperature.Units.MC, true);
            }

            int biomeCount = 0;
            for (BlockPos blockPos : world.dimensionType().hasCeiling() ? WorldHelper.getPositionCube(entPos, 6, 10) : WorldHelper.getPositionGrid(entPos, 36, 10))
            {
                if (!World.isInWorldBounds(blockPos) || blockPos.distSqr(entPos) > 30*30) continue;
                Biome biome = world.getBiomeManager().getBiome(blockPos);

                biomeCount++;
                ResourceLocation biomeID = biome.getRegistryName();

                double biomeVariance = 1 / Math.max(1, 2 + biome.getDownfall() * 2);
                double baseTemp = biome.getBaseTemperature();

                // Get the biome's temperature, either overridden by config or calculated
                // Start with biome override
                Triplet<Double, Double, Temperature.Units> cTemp = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeID,
                                                                   new Triplet<>(baseTemp - biomeVariance, baseTemp + biomeVariance, Temperature.Units.MC));
                Triplet<Double, Double, Temperature.Units> cOffset = ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeID,
                                                                     new Triplet<>(0d, 0d, Temperature.Units.MC));
                Pair<Double, Double> configTemp = CSMath.addPairs(Pair.of(cTemp.getFirst(), cTemp.getSecond()), Pair.of(cOffset.getFirst(), cOffset.getSecond()));

                // Biome temp at midnight (bottom of the sine wave)
                double min = configTemp.getFirst();
                // Biome temp at noon (top of the sine wave)
                double max = configTemp.getSecond();

                DimensionType dimension = world.dimensionType();
                if (!dimension.hasCeiling())
                {
                    double altitude = entity.getY();
                    double mid = (min + max) / 2;
                    // Biome temp with time of day
                    worldTemp += CSMath.blend(min, max, Math.sin(world.getDayTime() / (12000 / Math.PI)), -1, 1)
                              // Altitude calculation
                               + CSMath.blend(0, Math.min(-0.6, (min - mid) * 2), altitude, world.getSeaLevel(), world.getMaxBuildHeight());
                }
                // If dimension has ceiling (don't use time or altitude)
                else worldTemp += CSMath.average(max, min);
            }

            worldTemp /= Math.max(1, biomeCount);

            // Add dimension offset, if present
            Pair<Double, Temperature.Units> dimTempOffsetConf = ConfigSettings.DIMENSION_OFFSETS.get().get(dimensionID);
            if (dimTempOffsetConf != null)
            {   worldTemp += Temperature.convertUnits(dimTempOffsetConf.getFirst(), dimTempOffsetConf.getSecond(), Temperature.Units.MC, false);
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