package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraft.world.entity.player.Player;

public class HellLampTempModifier extends TempModifier
{
    @Override
    public Temperature getResult(Temperature temp, Player player)
    {
        player.getPersistentData().putDouble("preLampTemp", temp.get());

        double max = ConfigCache.getInstance().maxTemp;
        double min = ConfigCache.getInstance().minTemp;

        double mid = (max + min) / 2;
        return new Temperature(CSMath.blend(temp.get(), mid, 0.6, 0, 1));
    }

    @Override
    public String getID() {
        return "cold_sweat:hellspring_lamp";
    }
}
