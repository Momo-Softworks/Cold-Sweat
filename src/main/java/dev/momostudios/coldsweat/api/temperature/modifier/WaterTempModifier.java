package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
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
    {
        this.getNBT().putDouble("strength", strength);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        double worldTemp = Temperature.get(entity, Temperature.Type.WORLD);
        double maxTemp = ConfigSettings.MAX_TEMP.get();
        double minTemp = ConfigSettings.MIN_TEMP.get();

        double strength = this.getNBT().getDouble("strength");
        double returnRate = Math.min(-0.001, -0.001 - (worldTemp / 800));
        double addAmount = WorldHelper.isWet(entity) ? 0.01 : WorldHelper.isRainingAt(entity.level, entity.blockPosition()) ? 0.0025 : returnRate;
        double maxStrength = CSMath.clamp(Math.abs(CSMath.average(maxTemp, minTemp) - worldTemp) / 2, 0.23d, 0.5d);

        this.getNBT().putDouble("strength", CSMath.clamp(strength + addAmount, 0d, maxStrength));

        // If the strength is 0, this TempModifier expires
        if (strength <= 0.0)
        {
            this.expires(this.getTicksExisted() - 1);
        }

        if (!entity.isInWater())
        {
            if (Math.random() < Math.min(0.5, strength))
            {
                double randX = entity.getBbWidth() * (Math.random() - 0.5);
                double randY = (entity.getEyeHeight() * 0.8) * Math.random();
                double randZ = entity.getBbWidth() * (Math.random() - 0.5);
                entity.level.addParticle(ParticleTypes.FALLING_WATER, entity.getX() + randX, entity.getY() + randY, entity.getZ() + randZ, 0, 0, 0);
            }
        }

        return temp -> temp - this.getNBT().getDouble("strength");
    }

    @Override
    public String getID()
    {
        return "cold_sweat:water";
    }
}
