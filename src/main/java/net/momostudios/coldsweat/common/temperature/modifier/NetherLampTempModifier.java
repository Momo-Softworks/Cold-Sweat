package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ConfigCache;

public class NetherLampTempModifier extends TempModifier
{
    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        player.getPersistentData().putDouble("preLampTemp", temp.get());

        double max = ConfigCache.getInstance().maxTemp;
        double min = ConfigCache.getInstance().minTemp;

        double mid = (max + min) / 2;
        return mid + (temp.get() - mid) * 0.4;
    }

    @Override
    public String getID() {
        return "cold_sweat:netherbrine_lamp";
    }
}
