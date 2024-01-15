package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.Tags;
import oshi.util.tuples.Triplet;

import java.util.function.Function;

public class BiomeTempModifier extends TempModifier
{
    public BiomeTempModifier()
    {
        this(16);
    }

    public BiomeTempModifier(int samples)
    {   this.getNBT().putInt("Samples", samples);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        try
        {
            double worldTemp = 0;
            Level level = entity.level();
            BlockPos entPos = entity.blockPosition();

            // In the case that the dimension temperature is overridden by config, use that and skip everything else
            Pair<Double, Temperature.Units> dimTempOverride = ConfigSettings.DIMENSION_TEMPS.get().get(level.dimension().location());
            if (dimTempOverride != null)
            {   return temp -> temp + dimTempOverride.getFirst();
            }

            int biomeCount = 0;
            for (BlockPos blockPos : level.dimensionType().hasCeiling() ? WorldHelper.getPositionCube(entPos, 6, 10) : WorldHelper.getPositionGrid(entPos, 36, 10))
            {
                if (!level.isInWorldBounds(blockPos) || blockPos.distSqr(entPos) > 30*30) continue;
                Holder<Biome> holder = level.getBiomeManager().getBiome(blockPos);
                if (holder.is(Tags.Biomes.IS_UNDERGROUND)) continue;
                if (holder.unwrapKey().isEmpty()) continue;

                biomeCount++;
                Biome biome = holder.value();
                ResourceLocation biomeID = holder.unwrapKey().get().location();

                double biomeVariance = 1 / Math.max(1, 2 + biome.getModifiedClimateSettings().downfall() * 2);
                double baseTemp = biome.getBaseTemperature();

                // Get the biome's temperature, either overridden by config or calculated
                // Start with biome override
                Triplet<Double, Double, Temperature.Units> cTemp = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeID,
                                                                   new Triplet<>(baseTemp - biomeVariance, baseTemp + biomeVariance, Temperature.Units.MC));
                Triplet<Double, Double, Temperature.Units> cOffset = ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeID,
                                                                     new Triplet<>(0d, 0d, Temperature.Units.MC));
                Pair<Double, Double> configTemp = CSMath.addPairs(Pair.of(cTemp.getA(), cTemp.getB()), Pair.of(cOffset.getA(), cOffset.getB()));

                // Biome temp at midnight (bottom of the sine wave)
                double min = configTemp.getFirst();
                // Biome temp at noon (top of the sine wave)
                double max = configTemp.getSecond();

                DimensionType dimension = level.dimensionType();
                if (!dimension.hasCeiling())
                {
                    double altitude = entity.getY();
                    double mid = (min + max) / 2;
                    // Biome temp with time of day
                    double biomeTemp = CSMath.blend(min, max, Math.sin(level.getDayTime() / (12000 / Math.PI)), -1, 1)
                              // Altitude calculation
                               + CSMath.blend(0, Math.min(-0.6, (min - mid) * 2), altitude, level.getSeaLevel(), level.getMaxBuildHeight());
                    if (CompatManager.isPrimalWinterLoaded())// && holder.is(BiomeTags.IS_OVERWORLD))
                    {   biomeTemp = Math.min(biomeTemp, biomeTemp / 2) - Math.max(biomeTemp / 2, 0);
                    }
                    worldTemp += biomeTemp;
                }
                // If dimension has ceiling (don't use time or altitude)
                else worldTemp += CSMath.average(max, min);
            }

            worldTemp /= Math.max(1, biomeCount);

            // Add dimension offset, if present
            Pair<Double, Temperature.Units> dimTempOffsetConf = ConfigSettings.DIMENSION_OFFSETS.get().get(level.dimension().location());
            if (dimTempOffsetConf != null)
            {   worldTemp += dimTempOffsetConf.getFirst();
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