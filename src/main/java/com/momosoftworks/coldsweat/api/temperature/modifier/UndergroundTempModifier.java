package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.DepthTempData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UndergroundTempModifier extends TempModifier
{
    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        if (entity.level().dimensionType().hasCeiling()) return temp -> temp;

        BlockPos playerPos = BlockPos.containing(entity.getEyePosition());
        Level level = entity.level();

        List<Pair<BlockPos, Double>> depthTable = new ArrayList<>();

        // Collect a list of depths taken at regular intervals around the entity, and their distances from the player
        for (BlockPos pos : WorldHelper.getPositionGrid(entity.blockPosition(), 49, 10))
        {
            if (!level.isInWorldBounds(pos))
            {   continue;
            }
            depthTable.add(Pair.of(pos, Math.sqrt(pos.distSqr(playerPos))));
        }

        // Calculate the average temperature of underground biomes
        double biomeTempTotal = 0;
        int caveBiomeCount = 0;

        for (BlockPos pos : WorldHelper.getPositionCube(entity.blockPosition(), 5, 10))
        {
            if (!level.isInWorldBounds(pos)) continue;

            if (WorldHelper.getHeight(pos, level) <= entity.getY()) continue;

            // Get temperature of underground biomes
            Holder<Biome> holder = level.getBiomeManager().getBiome(pos);
            if (holder.is(Tags.Biomes.IS_UNDERGROUND))
            {
                if (holder.unwrapKey().isEmpty()) continue;
                Biome biome = holder.value();
                double baseTemp = biome.getBaseTemperature();

                Triplet<Double, Double, Temperature.Units> cTemp = ConfigSettings.BIOME_TEMPS.get(entity.registryAccess()).getOrDefault(biome, new Triplet<>(baseTemp, baseTemp, Temperature.Units.MC));
                Triplet<Double, Double, Temperature.Units> cOffset = ConfigSettings.BIOME_OFFSETS.get(entity.registryAccess()).getOrDefault(biome, new Triplet<>(0d, 0d, Temperature.Units.MC));

                double biomeTemp = CSMath.averagePair(Pair.of(cTemp.getA(), cTemp.getB()))
                                 + CSMath.averagePair(Pair.of(cOffset.getA(), cOffset.getB()));

                biomeTempTotal += biomeTemp;
                caveBiomeCount++;
            }
        }
        if (depthTable.isEmpty() && caveBiomeCount == 0)
        {   return temp -> temp;
        }

        int finalCaveBiomeCount = caveBiomeCount;
        double biomeTempAvg = biomeTempTotal / Math.max(1, caveBiomeCount);

        int skylight = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition());

        return temp ->
        {
            List<Pair<Double, Double>> depthTemps = new ArrayList<>();

            for (Pair<BlockPos, Double> pair : depthTable)
            {
                BlockPos.MutableBlockPos pos = pair.getFirst().mutable();
                double distance = pair.getSecond();

                // Fudge the height of the position to be influenced by skylight
                pos.move(0, skylight - 4, 0);
                // Get the depth region for this position
                Double depthTemp = null;
                for (DepthTempData tempData : ConfigSettings.DEPTH_REGIONS.get())
                {
                    if ((depthTemp = tempData.getTemperature(temp, pos, level)) != null)
                    {   break;
                    }
                }
                if (depthTemp == null) continue;
                double weight = 1 / (distance + 1);
                // Add the weighted temperature to the list
                depthTemps.add(new Pair<>(depthTemp, weight));
            }
            if (depthTemps.isEmpty()) return temp;
            // Calculate the weighted average of the depth temperatures
            double weightedDepthTemp = CSMath.weightedAverage(depthTemps);

            // Weigh the depth temperature against the number of underground biomes with temperature
            return CSMath.blend(weightedDepthTemp, biomeTempAvg, finalCaveBiomeCount, 0, depthTable.size());
        };
    }
}
