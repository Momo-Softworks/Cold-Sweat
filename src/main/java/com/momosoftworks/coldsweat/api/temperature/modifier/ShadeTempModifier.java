package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.function.Function;

public class ShadeTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        World level = entity.level;
        if (level.dimensionType().hasCeiling()) return temp -> temp;

        double darkness = 1 - (level.getBrightness(LightType.SKY, entity.blockPosition()) / 15.0);
        darkness *= Math.max(0, WorldHelper.getTimeMultiplier(level));
        double overcast = level.getRainLevel(1);
        double shade = Math.max(darkness, overcast);

        double shadeAmount = CSMath.blend(0, ConfigSettings.SHADE_TEMP_OFFSET.get(), shade, 0, 1);

        return temp -> temp + shadeAmount;
    }
}
