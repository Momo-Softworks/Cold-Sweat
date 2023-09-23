package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class WaterTempModifier extends TempModifier
{
    public WaterTempModifier()
    {
        this(0.01);
    }

    public WaterTempModifier(double strength)
    {   this.getNBT().putDouble("Strength", strength);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        double worldTemp = Temperature.get(entity, Temperature.Type.WORLD);
        double maxTemp = ConfigSettings.MAX_TEMP.get();
        double minTemp = ConfigSettings.MIN_TEMP.get();

        double strength = this.getNBT().getDouble("Strength");
        double returnRate = Math.min(-0.0012, -0.0012 - (worldTemp / 640));
        double addAmount = WorldHelper.isInWater(entity) ? 0.05 : WorldHelper.isRainingAt(entity.level, entity.blockPosition()) ? 0.0125 : returnRate;
        double maxStrength = CSMath.clamp(Math.abs(CSMath.average(maxTemp, minTemp) - worldTemp) / 2, 0.23d, 0.5d);

        double newStrength = CSMath.clamp(strength + addAmount, 0d, maxStrength);
        this.getNBT().putDouble("Strength", newStrength);

        // If the strength is 0, this TempModifier expires
        if (strength <= 0.0)
        {   this.expires(this.getTicksExisted() - 1);
        }

        return temp ->
        {
            if (!entity.isInWater())
            {
                if (Math.random() < strength * 2)
                {   double randX = entity.getBbWidth() * (Math.random() - 0.5);
                    double randY = entity.getBbHeight() * Math.random();
                    double randZ = entity.getBbWidth() * (Math.random() - 0.5);
                    entity.level.addParticle(ParticleTypes.FALLING_WATER, entity.getX() + randX, entity.getY() + randY, entity.getZ() + randZ, 0, 0, 0);
                }
            }
            return temp - newStrength;
        };
    }

    @Override
    public String getID()
    {
        return "cold_sweat:water";
    }
}
