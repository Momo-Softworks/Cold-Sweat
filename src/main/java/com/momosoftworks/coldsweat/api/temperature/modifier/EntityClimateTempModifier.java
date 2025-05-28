package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.EntityClimateData;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class EntityClimateTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        if (!ConfigSettings.ENTITY_CLIMATES.get().containsKey(entity.getType()))
        {   this.expires(0);
            return temp -> temp;
        }
        EntityClimateData climateData = ConfigHelper.getFirstOrNull(ConfigSettings.ENTITY_CLIMATES, entity.getType(), data -> data.test(entity));
        if (climateData != null && climateData.test(entity))
        switch (trait)
        {
            case WORLD :
            {   double worldTemp = WorldHelper.getRoughTemperatureAt(entity.level, entity.blockPosition(), 1);
                return temp -> temp + worldTemp;
            }
            case BURNING_POINT :
            {   return temp -> temp + climateData.getMaxOffset();
            }
            case FREEZING_POINT :
            {   return temp -> temp + climateData.getMinOffset();
            }
        }
        return temp -> temp;
    }
}
