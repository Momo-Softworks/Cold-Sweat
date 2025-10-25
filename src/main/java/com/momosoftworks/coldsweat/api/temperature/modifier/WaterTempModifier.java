package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class WaterTempModifier extends TempModifier
{
    public WaterTempModifier()
    {   this(-0.01);
    }

    public WaterTempModifier(double temperature)
    {   this.getNBT().putDouble("Temperature", temperature);
    }

    public double getTemperature()
    {   return this.getNBT().getDouble("Temperature");
    }

    public double getMaxTemperature(LivingEntity entity)
    {
        double worldTemp = Temperature.get(entity, Temperature.Trait.WORLD);
        double maxTemp = ConfigSettings.MAX_TEMP.get();
        double minTemp = ConfigSettings.MIN_TEMP.get();
        return CSMath.clamp(Math.abs(CSMath.average(maxTemp, minTemp) - worldTemp) / 2, 0.23d, 0.5d);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        boolean isWarm = entity.level.getBiome(entity.blockPosition()).is(ModBiomeTags.HAS_HOT_WATER);
        double worldTemp = Temperature.get(entity, Temperature.Trait.WORLD);
        double minWorldTemp = ConfigSettings.MIN_TEMP.get();
        double maxWorldTemp = ConfigSettings.MAX_TEMP.get();
        double configDrySpeed = ConfigSettings.DRYOFF_SPEED.get();

        double temperature = this.getTemperature();
        double addAmount = WorldHelper.isInWater(entity) ? ConfigSettings.WATER_SOAK_SPEED.get() * (isWarm ? 1 : -1) // In water
                         : WorldHelper.isRainingAt(entity.level, entity.blockPosition()) ? ConfigSettings.RAIN_SOAK_SPEED.get() // In rain
                         : 0;
        double dryAmount = CSMath.blendExp(configDrySpeed, configDrySpeed * 10, worldTemp, minWorldTemp, maxWorldTemp, 100);
        double maxTemp = this.getMaxTemperature(entity);

        double newTemperature = CSMath.clamp(CSMath.shrink(temperature + addAmount, dryAmount), -maxTemp, maxTemp);
        if (newTemperature == 0)
        {   this.expires(0);
        }

        this.getNBT().putDouble("Temperature", newTemperature);
        if (temperature != newTemperature)
        {   this.markDirty();
        }

        return temp -> temp + newTemperature;
    }

    @Override
    public void tick(LivingEntity entity)
    {
        if (entity.level.isClientSide() && ConfigSettings.WATER_EFFECT_SETTING.get().showParticles() && !entity.isInWater())
        {
            if (Math.random() < Math.abs(this.getNBT().getDouble("Temperature")) * 2)
            {
                double randX = entity.getBbWidth() * (Math.random() - 0.5);
                double randY = entity.getBbHeight() * Math.random();
                double randZ = entity.getBbWidth() * (Math.random() - 0.5);
                entity.level.addParticle(ParticleTypes.FALLING_WATER, entity.getX() + randX, entity.getY() + randY, entity.getZ() + randZ, 0, 0, 0);
            }
        }
    }
}
