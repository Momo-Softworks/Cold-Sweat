package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.DepthTempData;
import com.momosoftworks.coldsweat.data.codec.configuration.DimensionTempData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

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
        Level level = entity.level();

        // If a dimension temperature override is defined, return
        DimensionTempData dimTempOverride = ConfigSettings.DIMENSION_TEMPS.get(entity.level().registryAccess()).get(level.dimensionTypeRegistration());
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
        int skylight = entity.level().getBrightness(LightLayer.SKY, translatedPos);

        List<Pair<Elevation, RegionEntry>> depthRegions = new ArrayList<>(depthTable.size());

        for (Pair<BlockPos, Double> pair : depthTable)
        {
            BlockPos originalPos = pair.getFirst();
            int originalY = originalPos.getY();
            int minY = level.getMinBuildHeight();
            int groundLevel = WorldHelper.getHeight(originalPos, level);
            int adjustedY = CSMath.betweenInclusive(originalY, minY, groundLevel) ? CSMath.clamp(originalY + skylight - 4, minY, groundLevel)
                          : originalY >= groundLevel ? CSMath.clamp(originalY + skylight - 4, groundLevel, originalY)
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

    private record RegionEntry(DepthTempData.TempRegion region, double distance, int minY, int maxY)
    {}

    /**
     * Stores a BlockPos (pos) and the BlockPos after being offset by the light level (lightPos).
     */
    private record Elevation(BlockPos lightPos, BlockPos pos)
    {}
}
