package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.LightType;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.util.CSMath;
import net.momostudios.coldsweat.util.WorldHelper;

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
