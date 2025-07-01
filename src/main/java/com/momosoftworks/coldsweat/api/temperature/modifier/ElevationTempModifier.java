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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

        BlockPos translatedPos = CompatManager.Valkyrien.transformIfShipPos(level, entity.blockPosition());
        int skylight = entity.level.getBrightness(LightType.SKY, translatedPos);

        List<Pair<Elevation, RegionEntry>> depthRegions = new ArrayList<>(depthTable.size());

        for (Pair<BlockPos, Double> pair : depthTable)
        {
            BlockPos originalPos = pair.getFirst();
            int originalY = originalPos.getY();
            int minY = 0;
            int groundLevel = WorldHelper.getHeight(originalPos, level);
            int adjustedY = CSMath.betweenInclusive(originalY, minY, groundLevel) ? CSMath.clamp(originalY + skylight - 4, minY, groundLevel)
                          : originalY >= groundLevel ? CSMath.clamp(originalY + skylight - 4, groundLevel, originalY)
                          : CSMath.clamp(originalY + skylight - 4, originalY, minY);
            BlockPos pos = new BlockPos(originalPos.getX(), adjustedY, originalPos.getZ());
            double distance = pair.getSecond();
            findRegion:
            {
                for (DepthTempData data : ConfigSettings.DEPTH_REGIONS.get())
                {
                    DepthTempData.TempRegion region = data.getRegion(level, pos);
                    if (region == null) continue;
                    int regionMax = region.top().getHeight(pos, level);
                    int regionMin = region.bottom().getHeight(pos, level);
                    depthRegions.add(Pair.of(new Elevation(pos, originalPos), new RegionEntry(region, distance, regionMin, regionMax)));
                    break findRegion;
                }
                depthRegions.add(Pair.of(new Elevation(pos, originalPos), new RegionEntry(null, distance, 0, 0)));
            }
        }
        double midTemp = Temperature.getNeutralWorldTemp(entity);

        return temp ->
        {
            List<Pair<Double, Double>> depthTemps = new ArrayList<>();

            for (Pair<Elevation, RegionEntry> entry : depthRegions)
            {
                Elevation elevation = entry.getFirst();
                RegionEntry regionEntry = entry.getSecond();
                // Only use light for hot environments
                BlockPos pos = temp >= midTemp
                               ? elevation.lightPos()
                               : elevation.pos();
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

/**
     * Stores a BlockPos (pos) and the BlockPos after being offset by the light level (lightPos).
     */
    private static final class Elevation
    {
        private final BlockPos lightPos;
        private final BlockPos pos;

        private Elevation(BlockPos lightPos, BlockPos pos)
        {
            this.lightPos = lightPos;
            this.pos = pos;
        }

        public BlockPos lightPos() { return lightPos; }
        public BlockPos pos() { return pos; }
    }
}
