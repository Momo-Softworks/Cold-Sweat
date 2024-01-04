package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class MountTempModifier extends TempModifier
{
    public MountTempModifier()
    {   this(0, 0);
    }

    public MountTempModifier(double coldInsul, double hotInsul)
    {   this.getNBT().putDouble("ColdInsulation", coldInsul);
        this.getNBT().putDouble("HotInsulation", hotInsul);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        return temp ->
        {   String toChange = temp > 0 ? "HotInsulation" : "ColdInsulation";
            return CSMath.blend(temp, 0, this.getNBT().getDouble(toChange), 0, 1);
        };
    }

    public String getID()
    {
        return "cold_sweat:mount";
    }
}