package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DepthTempModifier extends TempModifier
{
    static int SAMPLES = 25;

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        if (player.level.dimensionType().hasCeiling()) return temp -> temp;

        double midTemp = (ConfigSettings.getInstance().maxTemp + ConfigSettings.getInstance().minTemp) / 2;
        BlockPos playerPos = player.blockPosition();

        Map<Integer, Double> lightLevels = new HashMap<>();
        Map<Double, Double> depthLevels = new HashMap<>();

        for (BlockPos pos : WorldHelper.getNearbyPositions(playerPos, SAMPLES, 3))
        {
            ChunkAccess chunk = player.level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.SURFACE, false);
            if (chunk == null) continue;
            double depth = Math.max(0, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - playerPos.getY());
            int light = player.level.getBrightness(LightLayer.SKY, pos);

            lightLevels.put(light, Math.max(0, 16 - Math.sqrt(pos.distSqr(playerPos))));
            depthLevels.put(depth, Math.max(0, 16 - Math.sqrt(pos.distSqr(playerPos))));
        }

        double light = CSMath.weightedAverage(lightLevels);
        double depth = CSMath.weightedAverage(depthLevels);

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
