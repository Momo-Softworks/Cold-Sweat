package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import earth.terrarium.ad_astra.common.item.armor.SpaceSuit;
import earth.terrarium.ad_astra.common.util.OxygenUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public class AdAstraOxygenTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        if (!entity.level.isClientSide)
        {
            Level level = entity.level;
            BlockPos pos = entity.blockPosition();
            // Space suit
            if (!OxygenUtils.posHasOxygen(level, pos) && SpaceSuit.hasOxygenatedSpaceSuit(entity))
            {   return temp -> CSMath.blend(temp, Temperature.getNeutralWorldTemp(entity), 0.75, 0, 1);
            }
            // Oxygen generator
            else if (!OxygenUtils.levelHasOxygen(level) && OxygenUtils.posHasOxygen(level, pos))
            {   return temp -> CSMath.blend(temp, Temperature.getNeutralWorldTemp(entity), ConfigSettings.THERMAL_SOURCE_STRENGTH.get() * 1.2, 0, 1);
            }
        }
        return temp -> temp;
    }
}
