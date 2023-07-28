package dev.momostudios.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UndergroundTempModifier extends TempModifier
{
    static int SAMPLES = 16;

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        boolean hasCeiling = entity.level.dimensionType().hasCeiling();

        double midTemp = (ConfigSettings.MAX_TEMP.get() + ConfigSettings.MIN_TEMP.get()) / 2;
        BlockPos playerPos = entity.blockPosition();
        Level level = entity.level;

        List<Pair<Double, Double>> depthTable = new ArrayList<>();

        double biomeTempTotal = 0;
        int caveBiomeCount = 0;

        for (BlockPos pos : WorldHelper.getPositionGrid(playerPos, SAMPLES, 8))
        {
            if (!level.isLoaded(pos)) continue;
            ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.SURFACE, false);
            if (chunk == null) continue;
            if (!hasCeiling) depthTable.add(Pair.of(Math.max(0d, WorldHelper.getHeight(pos, level) - playerPos.getY()), Math.sqrt(pos.distSqr(playerPos))));

            for (Holder<Biome> holder : List.of(level.getBiomeManager().getBiome(pos),
                                                level.getBiomeManager().getBiome(pos.above(12)),
                                                level.getBiomeManager().getBiome(pos.below(12))))
            {
                if (holder.is(Tags.Biomes.IS_UNDERGROUND) || hasCeiling)
                {
                    Biome biome = holder.value();
                    double baseTemp = biome.getBaseTemperature();
                    ResourceLocation biomeID = holder.unwrapKey().get().location();

                    Triplet<Double, Double, Temperature.Units> cTemp = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeID, new Triplet<>(baseTemp, baseTemp, Temperature.Units.MC));
                    Triplet<Double, Double, Temperature.Units> cOffset = ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeID, new Triplet<>(0d, 0d, Temperature.Units.MC));
                    double biomeTemp = CSMath.averagePair(Pair.of(cTemp.getA(), cTemp.getB()))
                                     + CSMath.averagePair(Pair.of(cOffset.getA(), cOffset.getB()));

                    biomeTempTotal += biomeTemp;
                    caveBiomeCount++;
                }
            }
        }

        double finalDepth = CSMath.weightedAverage(depthTable);
        int finalBiomeCount = Math.max(1, caveBiomeCount);
        double finalBiomeTempTotal = biomeTempTotal / finalBiomeCount;
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
