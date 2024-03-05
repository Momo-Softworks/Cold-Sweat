package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.Triplet;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
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
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        try
        {
            World world = entity.level;
            double worldTemp = 0;
            BlockPos entPos = entity.blockPosition();

            // In the case that the dimension temperature is overridden by config, use that and skip everything else
            Pair<Double, Temperature.Units> dimTempOverride = ConfigSettings.DIMENSION_TEMPS.get().get(world.dimension().location());
            if (dimTempOverride != null)
            {   return temp -> temp + dimTempOverride.getFirst();
            }

            // If there's a temperature structure here, ignore biome temp
            Double structureTemp = getStructureTemp(entity.level, entity.blockPosition());
            if (structureTemp != null)
            {   return temp -> structureTemp;
            }

            int biomeCount = 0;
            for (BlockPos blockPos : WorldHelper.getPositionGrid(entPos, 36, 10))
            {
                // Check if this position is valid
                if (!World.isInWorldBounds(blockPos) || blockPos.distSqr(entPos) > 30*30) continue;
                // Get the holder for the biome
                Biome biome = world.getBiomeManager().getBiome(blockPos);

                // Tally number of biomes
                biomeCount++;

                // Get min/max temperature of the biome
                Pair<Double, Double> configTemp = getBiomeTemp(biome);

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
                    double biomeTemp = CSMath.blend(min, max, Math.sin(world.getDayTime() / (12000 / Math.PI)), -1, 1)
                            // Altitude calculation
                            + CSMath.blend(0, Math.min(-0.6, (min - mid) * 2), altitude, world.getSeaLevel(), world.getMaxBuildHeight());
                    if (CompatManager.isPrimalWinterLoaded() && world.dimension().location().equals(DimensionType.OVERWORLD_LOCATION.location()))
                    {   biomeTemp = Math.min(biomeTemp, biomeTemp / 2) - Math.max(biomeTemp / 2, 0);
                    }
                    worldTemp += biomeTemp;
                }
                // If dimension has ceiling (don't use time or altitude)
                else worldTemp += CSMath.average(max, min);
            }

            worldTemp /= Math.max(1, biomeCount);

            // Add dimension offset, if present
            Pair<Double, Temperature.Units> dimTempOffsetConf = ConfigSettings.DIMENSION_OFFSETS.get().get(world.dimension().location());
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
    public Double getStructureTemp(World level, BlockPos pos)
    {
        if (!(level instanceof ServerWorld)) return null;

        ServerWorld serverLevel = ((ServerWorld) level);
        StructureManager structureManager = serverLevel.structureFeatureManager();
        Registry<Structure<?>> registry = level.registryAccess().registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY);

        // Iterate over all structures at the position (ignores Y level)
        for (Map.Entry<Structure<?>, LongSet> entry : level.getChunk(pos).getAllReferences().entrySet())
        {
            Structure<?> structure = entry.getKey();
            LongSet strucCoordinates = entry.getValue();

            // Iterate over all chunk coordinates within the structures
            for (long coordinate : strucCoordinates)
            {
                SectionPos sectionpos = SectionPos.of(new ChunkPos(coordinate), SectionPos.blockToSectionCoord(0));
                // Get the structure start
                StructureStart<?> structurestart = structureManager.getStartForFeature(sectionpos, structure, level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_STARTS));

                if (structurestart != null && structurestart.isValid() && structurestart.getBoundingBox().isInside(pos))
                {
                    // If the structure has a piece at the position, get the temperature
                    if (structurestart.getPieces().stream().anyMatch(piece -> piece.getBoundingBox().isInside(pos)))
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

    public Pair<Double, Double> getBiomeTemp(Biome biome)
    {
        double variance = 1 / Math.max(1, 2 + biome.getDownfall() * 2);
        ResourceLocation biomeID = ForgeRegistries.BIOMES.getKey(biome);
        double baseTemp = biome.getBaseTemperature();

        // Get the biome's temperature, either overridden by config or calculated
        // Start with biome override
        Triplet<Double, Double, Temperature.Units> configTemp = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeID,
                                                           new Triplet<>(baseTemp - variance, baseTemp + variance, Temperature.Units.MC));
        Triplet<Double, Double, Temperature.Units> configOffset = ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeID,
                                                             new Triplet<>(0d, 0d, Temperature.Units.MC));
        return CSMath.addPairs(Pair.of(configTemp.getFirst(), configTemp.getSecond()), Pair.of(configOffset.getFirst(), configOffset.getSecond()));
    }

    public String getID()
    {   return "cold_sweat:biome";
    }
}