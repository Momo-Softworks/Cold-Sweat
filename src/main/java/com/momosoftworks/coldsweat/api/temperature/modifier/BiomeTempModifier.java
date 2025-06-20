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
        BlockPos entPos = entity.blockPosition();

        // If a structure temperature override is defined, return
        Pair<Double, Double> structureTemp = getStructureTemp(entity.level(), entity.blockPosition());
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

            if (!dimension.hasCeiling() && !level.dimensionType().hasFixedTime())
            {
                if (CSMath.getIfNotNull(ConfigSettings.BIOME_TEMPS.get(level.registryAccess()).get(holder), BiomeTempData::isDisabled, false))
                {   continue;
                }
                // Biome temp with time of day
                double biomeTemp = WorldHelper.getBiomeTemperature(level, holder);

                // Primal Winter compat
                if (CompatManager.isPrimalWinterLoaded() && ConfigSettings.BIOME_TEMPS.get(level.registryAccess()).get(holder) != null)
                {
                    boolean isWinterBiome = ForgePrimalWinter.CONFIG.isWinterBiome(holder.unwrapKey().get());
                    boolean isWinterDimension = ForgePrimalWinter.CONFIG.isWinterDimension(level.dimension());
                    if (isWinterBiome && isWinterDimension)
                    {   biomeTemp = Math.min(biomeTemp, biomeTemp / 2);
                    }
                }
                // Add biome temperature
                worldTemp += biomeTemp;
            }
            // If dimension has ceiling (don't use time)
            else worldTemp += CSMath.averagePair(WorldHelper.getBiomeTemperatureRange(level, holder));

            // Tally number of biomes
            biomeCount++;
        }

        worldTemp /= Math.max(1, biomeCount);

        // Slightly decrease temperature if overcast
        if (!dimension.hasCeiling() && level.isRaining())
        {
            long time = level.getDayTime();
            double overcastTemp = ConfigSettings.OVERCAST_TEMP_OFFSET.get() * level.getRainLevel(1);
            worldTemp += CSMath.blend(0, overcastTemp, Math.abs(6000 - time), 6000, 0);
        }

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