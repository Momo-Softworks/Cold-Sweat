package dev.momosoftworks.coldsweat.api.temperature.modifier;

import dev.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class WaterskinTempModifier extends TempModifier
{
    public WaterskinTempModifier()
    {
        this(0.0);
    }

    public WaterskinTempModifier(double temp)
    {
        this.getNBT().putDouble("temperature", temp);
    }

    @Override
    public Function<Double, Double>  calculate(LivingEntity entity, Temperature.Type type)
    {
        return temp -> temp + this.getNBT().getDouble("temperature");
    }

    public String getID()
    {
        return "cold_sweat:waterskin";
    }
}