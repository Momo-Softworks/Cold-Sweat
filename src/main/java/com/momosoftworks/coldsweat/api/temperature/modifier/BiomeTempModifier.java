package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Triplet;

import java.util.Map;
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
            Pair<Double, Temperature.Units> dimTempOverride = ConfigSettings.DIMENSION_TEMPS.get().get(level.dimension().location());
            if (dimTempOverride != null)
            {   return temp -> temp + dimTempOverride.getFirst();
            }

            // If there's a temperature structure here, ignore biome temp
            Double structureTemp = getStructureTemp(entity.level, entity.blockPosition());
            if (structureTemp != null)
            {   return temp -> structureTemp;
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
                    if (CompatManager.isPrimalWinterLoaded() && holder.is(BiomeTags.IS_OVERWORLD))
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

    @Nullable
    public Double getStructureTemp(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        StructureManager structureManager = serverLevel.structureManager();
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);

        // Iterate over all structures at the position (ignores Y level)
        for (Map.Entry<Structure, LongSet> entry : structureManager.getAllStructuresAt(pos).entrySet())
        {
            Structure structure = entry.getKey();
            LongSet strucCoordinates = entry.getValue();

            // Iterate over all chunk coordinates within the structures
            for (long coordinate : strucCoordinates)
            {
                SectionPos sectionpos = SectionPos.of(new ChunkPos(coordinate), level.getMinSection());
                // Get the structure start
                StructureStart structurestart = structureManager.getStartForStructure(sectionpos, structure, level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_STARTS));

                if (structurestart != null && structurestart.isValid())
                {
                    // If the structure has a piece at the position, get the temperature
                    if (structureManager.structureHasPieceAt(pos, structurestart))
                    {
                        ResourceLocation structureId = registry.getKey(structure);
                        Pair<Double, Temperature.Units> strucTemp = ConfigSettings.STRUCTURE_TEMPS.get().get(structureId);

                        if (strucTemp != null)
                        {   return strucTemp.getFirst();
                        }
                    }
                }
            }
        }

        return null;
    }

    public Pair<Double, Double> getBiomeTemp(Holder<Biome> holder)
    {
        Biome biome = holder.value();
        double variance = 1 / Math.max(1, 2 + biome.getModifiedClimateSettings().downfall() * 2);
        ResourceLocation biomeID = holder.unwrapKey().get().location();
        double baseTemp = biome.getBaseTemperature();

        // Get the biome's temperature, either overridden by config or calculated
        // Start with biome override
        Triplet<Double, Double, Temperature.Units> configTemp = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeID,
                                                           new Triplet<>(baseTemp - variance, baseTemp + variance, Temperature.Units.MC));
        Triplet<Double, Double, Temperature.Units> configOffset = ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeID,
                                                             new Triplet<>(0d, 0d, Temperature.Units.MC));
        return CSMath.addPairs(Pair.of(configTemp.getA(), configTemp.getB()), Pair.of(configOffset.getA(), configOffset.getB()));
    }
}