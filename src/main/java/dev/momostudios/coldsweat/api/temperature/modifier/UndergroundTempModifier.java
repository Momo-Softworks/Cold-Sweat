package dev.momostudios.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class UndergroundTempModifier extends TempModifier
{
    static int SAMPLES = 16;

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        if (entity.level.dimensionType().hasCeiling()) return temp -> temp;

        double midTemp = (ConfigSettings.MAX_TEMP.get() + ConfigSettings.MIN_TEMP.get()) / 2;
        BlockPos playerPos = entity.blockPosition();
        Level level = entity.level;

        List<Pair<Double, Double>> depthTable = new ArrayList<>();

        double[] biomeTempTotal = new double[1];
        int[] caveBiomeCount = new int[1];

        for (BlockPos pos : WorldHelper.getPositionGrid(playerPos, SAMPLES, 8))
        {
            depthTable.add(Pair.of(Math.max(0d, WorldHelper.getHeight(pos, level) - playerPos.getY()), Math.sqrt(pos.distSqr(playerPos))));

            if (WorldHelper.getHeight(pos, level) <= entity.getY()) continue;

            Stream.of(level.getBiomeManager().getBiome(pos),
                      level.getBiomeManager().getBiome(pos.above(8)),
                      level.getBiomeManager().getBiome(pos.above(16)),
                      level.getBiomeManager().getBiome(pos.below(8)),
                      level.getBiomeManager().getBiome(pos.below(16))).distinct()
            .forEach(holder ->
            {
                if (holder.is(Tags.Biomes.IS_UNDERGROUND))
                {
                    Biome biome = holder.value();
                    double baseTemp = biome.getBaseTemperature();
                    ResourceLocation biomeID = holder.unwrapKey().get().location();

                    Triplet<Double, Double, Temperature.Units> cTemp = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeID, new Triplet<>(baseTemp, baseTemp, Temperature.Units.MC));
                    Triplet<Double, Double, Temperature.Units> cOffset = ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeID, new Triplet<>(0d, 0d, Temperature.Units.MC));
                    double biomeTemp = CSMath.averagePair(Pair.of(cTemp.getA(), cTemp.getB()))
                                     + CSMath.averagePair(Pair.of(cOffset.getA(), cOffset.getB()));

                    biomeTempTotal[0] += biomeTemp;
                    caveBiomeCount[0]++;
                }
            });
        }

        double finalDepth = CSMath.weightedAverage(depthTable);
        int finalBiomeCount = Math.max(1, caveBiomeCount[0]);
        double finalBiomeTempTotal = biomeTempTotal[0] / finalBiomeCount;
        return temp ->
        {
            double depthAvg = CSMath.weightedAverage(CSMath.blend(midTemp, temp, entity.level.getBrightness(LightLayer.SKY, entity.blockPosition()), 0, 15),
                                          CSMath.blend(temp, midTemp, finalDepth, 4, 20), 1, 2);
                return CSMath.blend(depthAvg, finalBiomeTempTotal, finalBiomeCount, 0, SAMPLES * 3);
        };
    }

    public String getID()
    {
        return "cold_sweat:height";
    }
}
