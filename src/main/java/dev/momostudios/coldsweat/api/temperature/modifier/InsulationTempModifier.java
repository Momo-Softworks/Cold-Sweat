package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class InsulationTempModifier extends TempModifier
{
    public InsulationTempModifier()
    {
        this(0d, 0d);
    }

    public InsulationTempModifier(double cold, double hot)
    {
        this.getNBT().putDouble("cold", cold);
        this.getNBT().putDouble("hot", hot);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        double cold = this.getNBT().getDouble("cold") / 8d;
        double hot = this.getNBT().getDouble("hot") / 8d;
        return temp ->
        {
            if (temp > 0)
            {   return hot >= 0 ? temp / (1 + hot * 2) : temp * (1 - hot * 2);
            }
            else return cold >= 0 ? temp / (1 + cold * 2) : temp * (1 - cold * 2);
        };
    }

    public String getID()
    {
        return "cold_sweat:armor";
    }
}