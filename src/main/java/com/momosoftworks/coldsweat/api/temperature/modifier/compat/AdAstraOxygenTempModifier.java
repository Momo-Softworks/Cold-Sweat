package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public class AdAstraOxygenTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        if (!entity.level().isClientSide)
        {
            Level level = entity.level();
            BlockPos pos = entity.blockPosition();
            // Space suit
            if (!OxygenApi.API.hasOxygen(level, pos) && SpaceSuitItem.getOxygenAmount(entity) > 0)
            {   return temp -> CSMath.blend(temp, Temperature.getNeutralWorldTemp(entity), 0.75, 0, 1);
            }
            // Oxygen generator
            else if (!OxygenApi.API.hasOxygen(level) && OxygenApi.API.hasOxygen(level, pos))
            {   return temp -> CSMath.blend(temp, Temperature.getNeutralWorldTemp(entity), ConfigSettings.THERMAL_SOURCE_STRENGTH.get() * 1.2, 0, 1);
            }
        }
        return temp -> temp;
    }
}
