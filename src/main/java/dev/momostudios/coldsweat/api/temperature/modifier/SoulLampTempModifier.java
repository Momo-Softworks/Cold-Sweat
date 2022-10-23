package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class SoulLampTempModifier extends TempModifier
{
    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        double almostMax = ConfigSettings.getInstance().maxTemp * 0.99;
        return temp ->
        {
            if (temp.get() < almostMax) return temp;

            return temp.multiply(Math.max(0.4, almostMax / temp.get()));
        };
    }

    @Override
    public String getID() {
        return "cold_sweat:soulspring_lamp";
    }
}
