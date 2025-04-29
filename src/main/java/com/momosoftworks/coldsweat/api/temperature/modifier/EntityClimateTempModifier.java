package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class EntityClimateTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        switch (trait)
        {
            case WORLD ->
            {   double worldTemp = WorldHelper.getRoughTemperatureAt(entity.level, entity.blockPosition(), true);
                return temp -> temp + worldTemp;
            }
            case BURNING_POINT ->
            {   return temp -> temp + ConfigSettings.ENTITY_CLIMATES.get().get(entity.getType()).getMaxTemp();
            }
            case FREEZING_POINT ->
            {   return temp -> temp + ConfigSettings.ENTITY_CLIMATES.get().get(entity.getType()).getMinTemp();
            }
        }
        return temp -> temp;
    }
}
