package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.WorldHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.LightType;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.config.ConfigCache;

import java.util.HashMap;
import java.util.Map;

public class DepthTempModifier extends TempModifier
{
    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        double midTemp = (ConfigCache.getInstance().maxTemp + ConfigCache.getInstance().minTemp) / 2;
        double depth = Math.max(0, WorldHelper.getGroundLevel(player.getPosition(), player.world) - player.getPosition().getY());

        Map<Double, Double> valueMap = new HashMap<>();
        valueMap.put(temp.get(), 0.8);
        valueMap.put(CSMath.blend(midTemp, temp.get(), player.world.getLightFor(LightType.SKY, player.getPosition()), 0, 15), 2.0);
        valueMap.put(CSMath.blend(temp.get(), midTemp, !WorldHelper.canSeeSky(player.world, player.getPosition()) ? depth : depth / 2, 2, 20), 4.0);

        return CSMath.weightedAverage(valueMap);
    }

    public String getID()
    {
        return "cold_sweat:depth";
    }
}
