package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.alcatrazescapee.primalwinter.ForgePrimalWinter;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.codec.configuration.BiomeTempData;
import com.momosoftworks.coldsweat.data.codec.configuration.DimensionTempData;
import com.momosoftworks.coldsweat.data.codec.configuration.StructureTempData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.Tags;

import java.util.Optional;
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
        int samples = this.getNBT().getInt("Samples");
        double worldTemp = 0;
        Level level = entity.level();
        DimensionType dimension = level.dimensionType();
        BlockPos entPos = WorldHelper.sublevelToWorld(level, entity.blockPosition());

        // If a structure temperature override is defined, return
        Pair<Double, Double> structureTemp = getStructureTemp(entity.level(), entPos);
        if (structureTemp.getFirst() != null)
        {   return temp -> structureTemp.getFirst();
        }

        // If the dimension temperature is overridden, return
        DimensionTempData dimTempOverride = ConfigSettings.DIMENSION_TEMPS.get(level.registryAccess()).get(level.dimensionTypeRegistration());
        if (dimTempOverride != null)
        {   return temp -> temp + dimTempOverride.getTemperature();
        }

        DimensionTempData dimTempOffset = ConfigSettings.DIMENSION_OFFSETS.get(level.registryAccess()).get(level.dimensionTypeRegistration());
        double dimOffset = dimTempOffset != null ? dimTempOffset.getTemperature() : 0;

        int biomeCount = 0;
        for (BlockPos blockPos : dimension.hasCeiling() ? WorldHelper.getPositionCube(entPos, (int) Math.sqrt(samples), 10) : WorldHelper.getPositionGrid(entPos, samples, 10))
        {
            // Get the holder for the biome
            Holder<Biome> holder = level.getBiomeManager().getBiome(blockPos);
            if (holder.is(Tags.Biomes.IS_UNDERGROUND)) continue;
            if (holder.unwrapKey().isEmpty()) continue;

            BiomeTempData biomeTempData = ConfigSettings.BIOME_TEMPS.get(level.registryAccess()).get(holder);
            if (biomeTempData != null && biomeTempData.isDisabled())
            {   continue;
            }
            // Biome temp with time of day
            double biomeTemp = WorldHelper.getBiomeTemperature(level, holder);

            // Primal Winter compat for configured biomes
            if (CompatManager.isPrimalWinterLoaded() && biomeTempData != null)
            {
                boolean isWinterBiome = ForgePrimalWinter.CONFIG.isWinterBiome(holder.unwrapKey().get());
                boolean isWinterDimension = ForgePrimalWinter.CONFIG.isWinterDimension(level.dimension());
                if (isWinterBiome && isWinterDimension)
                {   biomeTemp = -0.5;
                }
            }
            // Add biome temperature
            worldTemp += biomeTemp;

            // Tally number of biomes
            biomeCount++;
        }
        if (biomeCount == 0)
        {   worldTemp = CSMath.average(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get());
        }

        worldTemp /= Math.max(1, biomeCount);

        // Add structure offset, if present
        worldTemp += structureTemp.getSecond();

        double finalWorldTemp = worldTemp;
        return temp -> temp + finalWorldTemp + dimOffset;
    }

    public static Pair<Double, Double> getStructureTemp(Level level, BlockPos pos)
    {
        Optional<Holder<Structure>> structure = WorldHelper.getStructureAt(level, pos);
        if (structure.isEmpty()) return Pair.of(null, 0d);

        Double strucTemp = CSMath.getIfNotNull(ConfigSettings.STRUCTURE_TEMPS.get(level.registryAccess()).get(structure.get()), StructureTempData::getTemperature, null);
        Double strucOffset = CSMath.getIfNotNull(ConfigSettings.STRUCTURE_OFFSETS.get(level.registryAccess()).get(structure.get()), StructureTempData::getTemperature, 0d);

        return Pair.of(strucTemp, strucOffset);
    }
}