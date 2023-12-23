package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class HearthTempModifier extends TempModifier
{
    public HearthTempModifier()
    {
       this(0);
    }

    public HearthTempModifier(int strength)
    {
        this.getNBT().putInt("Strength", strength);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        double min = ConfigSettings.MIN_TEMP.get();
        double max = ConfigSettings.MAX_TEMP.get();
        double mid = (min + max) / 2;
        double hearthStrength = ConfigSettings.HEARTH_STRENGTH.get();

        int insulationStrength = this.getNBT().getInt("Strength");

        return temp -> CSMath.blend(temp, CSMath.weightedAverage(temp, mid, 1 - hearthStrength, 1.0), insulationStrength, 0, 10);
    }

    public String getID()
    {
        return "cold_sweat:hearth";
    }
}