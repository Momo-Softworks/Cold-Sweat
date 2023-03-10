package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class SoulLampTempModifier extends TempModifier
{
    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        double almostMax = ConfigSettings.MAX_TEMP.get() * 0.99;
        return temp ->
        {
            if (temp < almostMax) return temp;

            return temp * (Math.max(0.4, almostMax / temp));
        };
    }

    @Override
    public String getID() {
        return "cold_sweat:soulspring_lamp";
    }
}
