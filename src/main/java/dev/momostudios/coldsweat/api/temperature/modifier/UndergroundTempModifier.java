package dev.momostudios.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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

        double biomeTempTotal = 0;
        int caveBiomeCount = 0;
        int totalBiomes = 0;

        for (BlockPos pos : WorldHelper.getPositionGrid(playerPos, SAMPLES, 8))
        {
            ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.SURFACE, false);
            if (chunk == null) continue;
            depthTable.add(Pair.of(Math.max(0d, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - playerPos.getY()), Math.sqrt(pos.distSqr(playerPos))));

            List<Holder<Biome>> biomes =  Stream.of(level.getBiomeManager().getBiome(pos),
                                                    level.getBiomeManager().getBiome(pos.above(12)),
                                                    level.getBiomeManager().getBiome(pos.below(12))).distinct().toList();
            totalBiomes += biomes.size();
            for (Holder<Biome> holder : biomes)
            {
                if (holder.is(Tags.Biomes.IS_UNDERGROUND))
                {
                    Biome biome = holder.value();
                    double baseTemp = biome.getBaseTemperature();
                    double biomeTemp = CSMath.averagePair(ConfigSettings.BIOME_TEMPS.get().getOrDefault(biome.getRegistryName(), Pair.of(baseTemp, baseTemp)))
                                     + CSMath.averagePair(ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biome.getRegistryName(), Pair.of(0d, 0d)));

                    biomeTempTotal += biomeTemp;
                    caveBiomeCount++;
                }
            }
        }

        double finalDepth = CSMath.weightedAverage(depthTable);
        int finalBiomeCount = Math.max(1, caveBiomeCount);
        double finalBiomeTempTotal = biomeTempTotal / finalBiomeCount;
        int finalTotalBiomes = totalBiomes;
        return temp ->
        {
            double depthAvg = CSMath.weightedAverage(CSMath.blend(midTemp, temp, entity.level.getBrightness(LightLayer.SKY, entity.blockPosition()), 0, 15),
                                          CSMath.blend(temp, midTemp, finalDepth, 4, 20), 1, 2);
                return CSMath.blend(depthAvg, finalBiomeTempTotal, finalBiomeCount, 0, finalTotalBiomes);
        };
    }

    public String getID()
    {
        return "cold_sweat:height";
    }
}
