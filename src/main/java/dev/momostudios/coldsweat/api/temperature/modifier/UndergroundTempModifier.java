package dev.momostudios.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UndergroundTempModifier extends TempModifier
{
    static int SAMPLES = 16;

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        if (entity.level.dimensionType().hasCeiling()) return temp -> temp;

        double midTemp = (ConfigSettings.MAX_TEMP.get() + ConfigSettings.MIN_TEMP.get()) / 2;
        BlockPos playerPos = entity.blockPosition();
        World world = entity.level;

        List<Pair<Double, Double>> depthTable = new ArrayList<>();

        for (BlockPos pos : WorldHelper.getPositionGrid(playerPos, SAMPLES, 8))
        {
            depthTable.add(Pair.of(Math.max(0d, WorldHelper.getHeight(pos, world) - playerPos.getY()), Math.sqrt(pos.distSqr(playerPos))));

            if (WorldHelper.getHeight(pos, world) <= entity.getY()) continue;
        }

        double finalDepth = CSMath.weightedAverage(depthTable);
        return temp ->
        {
            return CSMath.weightedAverage(CSMath.blend(midTemp, temp, entity.level.getBrightness(LightType.SKY, entity.blockPosition()), 0, 15),
                                          CSMath.blend(temp, midTemp, finalDepth, 4, 20), 1, 2);
        };
    }

    public String getID()
    {
        return "cold_sweat:height";
    }
}
