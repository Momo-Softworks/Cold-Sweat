package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.DepthTempData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UndergroundTempModifier extends TempModifier
{
    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        if (entity.level.dimensionType().hasCeiling()) return temp -> temp;

        double midTemp = (ConfigSettings.MAX_TEMP.get() + ConfigSettings.MIN_TEMP.get()) / 2;
        BlockPos playerPos = new BlockPos(entity.getEyePosition(0));
        World level = entity.level;

        // Depth, Weight
        List<Pair<BlockPos, Double>> depthTable = new ArrayList<>();

        // Collect a list of depths taken at regular intervals around the entity, and their distances from the player
        for (BlockPos pos : WorldHelper.getPositionGrid(entity.blockPosition(), 49, 10))
        {
            if (!level.isInWorldBounds(pos))
            {   continue;
            }
            depthTable.add(Pair.of(pos, Math.sqrt(pos.distSqr(playerPos))));
        }
        if (depthTable.isEmpty())
        {   return temp -> temp;
        }

        int skylight = entity.level.getBrightness(LightType.SKY, entity.blockPosition());

        return temp ->
        {
            List<Pair<Double, Double>> depthTemps = new ArrayList<>();

            for (Pair<BlockPos, Double> pair : depthTable)
            {
                BlockPos.Mutable pos = pair.getFirst().mutable();
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
            // Calculate the weighted average of the depth temperatures
            double weightedDepthTemp = CSMath.weightedAverage(depthTemps);

            // Weigh the depth temperature against the number of underground biomes with temperature
            return CSMath.blend(temp, weightedDepthTemp, ConfigSettings.CAVE_INSULATION.get(), 0, 1);
        };
    }
}
