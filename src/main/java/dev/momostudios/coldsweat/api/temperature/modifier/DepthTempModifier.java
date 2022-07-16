package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DepthTempModifier extends TempModifier
{
    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        if (player.level.dimensionType().hasCeiling()) return temp -> temp;

        double midTemp = (ConfigCache.getInstance().maxTemp + ConfigCache.getInstance().minTemp) / 2;
        BlockPos playerPos = player.blockPosition();

        List<Integer> lightLevels = new ArrayList<>();
        List<Double> depthLevels = new ArrayList<>();

        for (BlockPos pos : WorldHelper.getNearbyPositions(playerPos, 25, 5))
        {
            double depth = Math.max(0, player.level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - playerPos.getY());
            int light = player.level.getBrightness(LightLayer.SKY, pos);

            lightLevels.add(light);
            depthLevels.add(depth);
        }

        double light = CSMath.average(lightLevels.toArray(new Integer[0]));
        double depth = CSMath.average(depthLevels.toArray(new Double[0]));

        return temp ->
        {
            Map<Double, Double> valueMap = new HashMap<>();
            valueMap.put(temp.get(), 0.8);
            valueMap.put(CSMath.blend(midTemp, temp.get(), light, 0, 15), 2.0);
            valueMap.put(CSMath.blend(temp.get(), midTemp, depth, 4, 20), 4.0);

            return new Temperature(CSMath.weightedAverage(valueMap));
        };
    }

    public String getID()
    {
        return "cold_sweat:depth";
    }
}
