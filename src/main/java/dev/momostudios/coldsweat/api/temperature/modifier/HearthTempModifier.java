package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
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
        this.getNBT().putInt("strength", strength);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        double min = ConfigSettings.MIN_TEMP.get();
        double max = ConfigSettings.MAX_TEMP.get();
        double mid = (min + max) / 2;
        double hearthStrength = ColdSweatConfig.getInstance().getHearthEffect();

        int insulationStrength = this.getNBT().getInt("strength");

        return temp -> CSMath.blend(temp, CSMath.weightedAverage(temp, mid, 1 - hearthStrength, 1.0), insulationStrength, 0, 10);
    }

    public String getID()
    {
        return "cold_sweat:hearth";
    }
}