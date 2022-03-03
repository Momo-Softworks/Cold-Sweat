package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;

import java.util.HashMap;
import java.util.Map;

public class DepthTempModifier extends TempModifier
{
    @Override
    public double getResult(Temperature temp, Player player)
    {
        double midTemp = (ConfigCache.getInstance().maxTemp + ConfigCache.getInstance().minTemp) / 2;
        double depth = Math.max(0, WorldHelper.getGroundLevel(player.blockPosition(), player.level) - player.blockPosition().getY());

        Map<Double, Double> valueMap = new HashMap<>();
        valueMap.put(temp.get(), 0.8);
        valueMap.put(CSMath.blend(midTemp, temp.get(), player.level.getBrightness(LightLayer.SKY, player.blockPosition()), 0, 15), 2.0);
        valueMap.put(CSMath.blend(temp.get(), midTemp, !WorldHelper.canSeeSky(player.level, player.blockPosition()) ? depth : depth / 2, 2, 20), 4.0);

        return CSMath.weightedAverage(valueMap);
    }

    public String getID()
    {
        return "cold_sweat:depth";
    }
}
