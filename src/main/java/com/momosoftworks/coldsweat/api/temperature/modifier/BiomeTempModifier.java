package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
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
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        try
        {
            double worldTemp = 0;
            Level level = entity.level;
            BlockPos entPos = entity.blockPosition();

            // In the case that the dimension temperature is overridden by config, use that and skip everything else
            Pair<Double, Temperature.Units> dimTempOverride = ConfigSettings.DIMENSION_TEMPS.get().get(level.dimensionType());
            if (dimTempOverride != null)
            {   return temp -> temp + dimTempOverride.getFirst();
            }

            // If there's a temperature structure here, ignore biome temp
            Pair<Double, Double> structureTemp = getStructureTemp(entity.level, entity.blockPosition());
            if (structureTemp.getFirst() != null)
            {   return temp -> structureTemp.getFirst();
            }

            int biomeCount = 0;
            for (BlockPos blockPos : level.dimensionType().hasCeiling() ? WorldHelper.getPositionCube(entPos, 6, 10) : WorldHelper.getPositionGrid(entPos, 36, 10))
            {
                // Check if this position is valid
                if (!level.isInWorldBounds(blockPos) || blockPos.distSqr(entPos) > 30*30) continue;
                // Get the holder for the biome
                Holder<Biome> holder = level.getBiomeManager().getBiome(blockPos);
                if (holder.is(Tags.Biomes.IS_UNDERGROUND)) continue;
                if (holder.unwrapKey().isEmpty()) continue;

                // Tally number of biomes
                biomeCount++;

                // Get min/max temperature of the biome
                Pair<Double, Double> configTemp = getBiomeTemp(holder);

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
                    if (CompatManager.isPrimalWinterLoaded() && level.dimensionTypeRegistration().is(DimensionType.OVERWORLD_LOCATION))
                    {   biomeTemp = Math.min(biomeTemp, biomeTemp / 2) - Math.max(biomeTemp / 2, 0);
                    }
                    worldTemp += biomeTemp;
                }
                // If dimension has ceiling (don't use time or altitude)
                else worldTemp += CSMath.average(max, min);
            }

            worldTemp /= Math.max(1, biomeCount);

            // Add dimension offset, if present
            Pair<Double, Temperature.Units> dimTempOffsetConf = ConfigSettings.DIMENSION_OFFSETS.get().get(level.dimensionType());
            if (dimTempOffsetConf != null)
            {   worldTemp += dimTempOffsetConf.getFirst();
            }

            // Add structure offset, if present
            worldTemp += structureTemp.getSecond();

            double finalWorldTemp = worldTemp;
            return temp -> temp + finalWorldTemp;
        }
        catch (Exception e)
        {   return temp -> temp;
        }
    }

    public Pair<Double, Double> getStructureTemp(Level level, BlockPos pos)
    {
        ConfiguredStructureFeature<?, ?> structure = WorldHelper.getStructureAt(level, pos);
        if (structure == null) return Pair.of(null, 0d);

        Double strucTemp = CSMath.getIfNotNull(ConfigSettings.STRUCTURE_TEMPS.get().get(structure.feature), Pair::getFirst, null);
        Double strucOffset = CSMath.getIfNotNull(ConfigSettings.STRUCTURE_OFFSETS.get().get(structure.feature), Pair::getFirst, 0d);

        return Pair.of(strucTemp, strucOffset);
    }

    public Pair<Double, Double> getBiomeTemp(Holder<Biome> holder)
    {
        Biome biome = holder.value();
        double variance = 1 / Math.max(1, 2 + biome.getDownfall() * 2);
        double baseTemp = biome.getBaseTemperature();

        // Get the biome's temperature, either overridden by config or calculated
        // Start with biome override
        Triplet<Double, Double, Temperature.Units> configTemp = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biome,
                                                           new Triplet<>(baseTemp - variance, baseTemp + variance, Temperature.Units.MC));
        Triplet<Double, Double, Temperature.Units> configOffset = ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biome,
                                                             new Triplet<>(0d, 0d, Temperature.Units.MC));
        return CSMath.addPairs(Pair.of(configTemp.getA(), configTemp.getB()), Pair.of(configOffset.getA(), configOffset.getB()));
    }
}