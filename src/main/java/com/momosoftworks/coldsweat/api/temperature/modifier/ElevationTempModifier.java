package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.DepthTempData;
import com.momosoftworks.coldsweat.data.codec.configuration.DimensionTempData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ElevationTempModifier extends TempModifier
{
    public ElevationTempModifier()
    {   this(49);
    }

    public ElevationTempModifier(int samples)
    {   this.getNBT().putInt("Samples", samples);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        World level = entity.level;

        // If a dimension temperature override is defined, return
        DimensionTempData dimTempOverride = ConfigSettings.DIMENSION_TEMPS.get(entity.level.registryAccess()).get(level.dimensionType());
        if (dimTempOverride != null)
        {   return temp -> temp;
        }
        // Don't calculate elevation for roofed dimensions
        if (level.dimensionType().hasCeiling()) return temp -> temp;

        // Collect a list of depths taken at regular intervals around the entity, and their distances from the player
        List<Pair<BlockPos, Double>> depthTable = new ArrayList<>();
        for (BlockPos pos : WorldHelper.getPositionGrid(entity.blockPosition(), this.getNBT().getInt("Samples"), 10))
        {
            depthTable.add(Pair.of(pos, CSMath.getDistance(entity.blockPosition(), pos)));
        }

        BlockPos translatedPos = CompatManager.isValkyrienSkiesLoaded()
                                 ? CompatManager.Valkyrien.transformIfShipPos(level, entity.blockPosition())
                                 : entity.blockPosition();
        int normalSkylight = entity.level.getBrightness(LightType.SKY, entity.blockPosition());
        int translatedSkylight = entity.level.getBrightness(LightType.SKY, translatedPos);
        int skylight = Math.min(normalSkylight, translatedSkylight);

        List<Pair<BlockPos, RegionEntry>> depthRegions = new ArrayList<>(depthTable.size());

        for (Pair<BlockPos, Double> pair : depthTable)
        {
            BlockPos originalPos = pair.getFirst();
            int originalY = originalPos.getY();
            int minY = 0;
            int groundLevel = WorldHelper.getHeight(originalPos, level, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES);
                            // If original is between bedrock and ground level, clamp to those bounds
            int adjustedY = CSMath.betweenInclusive(originalY, minY, groundLevel) ? CSMath.clamp(originalY + skylight - 4, minY, groundLevel)
                            // If original is above ground level, clamp to above ground level and below original
                          : originalY >= groundLevel ? CSMath.clamp(originalY + skylight - 4, groundLevel, originalY)
                            // If original is below bedrock, clamp to below bedrock and above original
                          : CSMath.clamp(originalY + skylight - 4, originalY, minY);
            BlockPos pos = new BlockPos(originalPos.getX(), adjustedY, originalPos.getZ());
            double distance = pair.getSecond();
            findRegion:
            {
                for (DepthTempData data : ConfigSettings.DEPTH_REGIONS.get().get(level.dimensionType()))
                {
                    DepthTempData.TempRegion region = data.getRegion(level, pos);
                    if (region == null) continue;
                    int regionMax = region.top().getHeight(pos, level);
                    int regionMin = region.bottom().getHeight(pos, level);
                    depthRegions.add(Pair.of(pos, new RegionEntry(region, distance, regionMin, regionMax)));
                    break findRegion;
                }
                depthRegions.add(Pair.of(pos, new RegionEntry(null, distance, 0, 0)));
            }
        }
        return temp ->
        {
            List<Pair<Double, Double>> depthTemps = new ArrayList<>();

            for (Pair<BlockPos, RegionEntry> entry : depthRegions)
            {
                BlockPos pos = entry.getFirst();
                RegionEntry regionEntry = entry.getSecond();
                // Get the region and distance
                DepthTempData.TempRegion region = regionEntry.region();
                if (region != null)
                {
                    double distance = regionEntry.distance();
                    int maxY = regionEntry.maxY();
                    int minY = regionEntry.minY();

                    double depthTemp = region.getTemperature(temp, pos, level, maxY, minY);
                    double weight = 1 / (distance / 10 + 1);
                    // Add the weighted temperature to the list
                    depthTemps.add(new Pair<>(depthTemp, weight));
                }
            }
            if (depthTemps.isEmpty())
            {   return temp;
            }
            // Calculate the weighted average of the depth temperatures
            return CSMath.weightedAverage(depthTemps);
        };
    }

    private static final class RegionEntry
    {
        private final DepthTempData.TempRegion region;
        private final double distance;
        private final int minY;
        private final int maxY;

        private RegionEntry(DepthTempData.TempRegion region, double distance, int minY, int maxY)
        {
            this.region = region;
            this.distance = distance;
            this.minY = minY;
            this.maxY = maxY;
        }

        public DepthTempData.TempRegion region() { return region; }
        public double distance() { return distance; }
        public int minY() { return minY; }
        public int maxY() { return maxY; }
    }
}
