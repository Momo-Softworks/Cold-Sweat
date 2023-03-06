package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class FireTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        return temp -> entity.isOnFire() ? temp + 10 : temp;
    }

    @Override
    public String getID()
    {
        return "cold_sweat:on_fire";
    }
}
