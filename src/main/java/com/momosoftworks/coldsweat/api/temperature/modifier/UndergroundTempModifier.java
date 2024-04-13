package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
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
        List<Pair<Double, Double>> depthTable = new ArrayList<>();

        double biomeTempTotal = 0;
        int caveBiomeCount = 0;

        for (BlockPos pos : WorldHelper.getPositionGrid(entity.blockPosition(), 5, 10))
        {   depthTable.add(Pair.of(Math.max(0d, WorldHelper.getHeight(pos, level) - playerPos.getY()),
                                   Math.sqrt(pos.distSqr(playerPos))));
        }
        if (depthTable.isEmpty()) return temp -> temp;

        double depth = CSMath.blend(0, CSMath.weightedAverage(depthTable), ConfigSettings.CAVE_INSULATION.get(), 0, 1);
        int biomeCount = Math.max(1, caveBiomeCount);
        double biomeTempAvg = biomeTempTotal / biomeCount;
        return temp ->
        {
            double depthAvg = CSMath.weightedAverage(CSMath.blend(midTemp, temp, entity.level.getBrightness(LightType.SKY, entity.blockPosition()), 0, 15),
                                          CSMath.blend(temp, midTemp, depth, 4, 20), 1, 2);
            return CSMath.blend(depthAvg, biomeTempAvg, biomeCount, 0, depthTable.size());
        };
    }

    public String getID()
    {
        return "cold_sweat:height";
    }
}
