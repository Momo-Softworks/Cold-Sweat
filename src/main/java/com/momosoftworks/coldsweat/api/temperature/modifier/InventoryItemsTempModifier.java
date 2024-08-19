package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class InventoryItemsTempModifier extends TempModifier
{
    public InventoryItemsTempModifier(double temp)
    {   this.getNBT().putDouble("Effect", temp);
    }

    public InventoryItemsTempModifier()
    {   this(0);
    }

    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        return temp -> temp + this.getNBT().getDouble("Effect");
    }
}
