package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;

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

    public void setTemperature(double temperature)
    {
        if (temperature != this.getTemperature())
        {   this.markDirty();
        }
        this.getNBT().putDouble("Temperature", temperature);
    }

    public double getTargetTemperature(LivingEntity entity)
    {
        BlockPos entityPos = WorldHelper.sublevelToWorld(entity.level, entity.blockPosition());
        Double[] waterTemps = WorldHelper.getPositionGrid(entityPos, 9, 4).stream()
                              .map(pos -> WorldHelper.getWaterTemperatureDelta(entity.level, pos))
                              .toArray(Double[]::new);
        return CSMath.average(waterTemps);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        double temperature = this.getTemperature();
        double tickRate = this.getTickRate() / 5.0;
        double newTemperature;
        if (WorldHelper.isInWater(entity))
        {   newTemperature = CSMath.approach(temperature, this.getTargetTemperature(entity), ConfigSettings.WATER_SOAK_SPEED.get() * tickRate);
        }
        else if (WorldHelper.isRainingAt(entity.level, entity.blockPosition()))
        {
            double maxRainEffect = -ConfigSettings.MAX_RAIN_SOAK.get();
            newTemperature = temperature > maxRainEffect
                           ? CSMath.approach(temperature, maxRainEffect, ConfigSettings.RAIN_SOAK_SPEED.get() * tickRate)
                           : temperature;
        }
        else
        {
            double worldTemp = Temperature.get(entity, Temperature.Trait.WORLD);
            double configDrySpeed = ConfigSettings.DRYOFF_SPEED.get();
            double dryAmount = CSMath.blendExp(configDrySpeed / 1.5, configDrySpeed * 5, worldTemp, ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get(), 20);
            newTemperature = CSMath.shrink(temperature, dryAmount * tickRate);
        }
        if (newTemperature == 0)
        {   this.expires(0);
        }
        this.setTemperature(newTemperature);

        return temp -> temp + newTemperature;
    }

    @Override
    public void tick(LivingEntity entity)
    {
        if (entity.level.isClientSide() && ConfigSettings.WATER_EFFECT_SETTING.get().showParticles() && !entity.isInWater()
        && Math.random() < Math.abs(this.getTemperature()) * 2 && Temperature.getImmunityToModifier(entity, this) < 1.0)
        {
            double randX = entity.getBbWidth() * (Math.random() - 0.5);
            double randY = entity.getBbHeight() * Math.random();
            double randZ = entity.getBbWidth() * (Math.random() - 0.5);
            entity.level.addParticle(ParticleTypes.FALLING_WATER, entity.getX() + randX, entity.getY() + randY, entity.getZ() + randZ, 0, 0, 0);
        }
        if (!entity.level.isClientSide && entity.isOnFire())
        {
            this.setTemperature(CSMath.shrink(this.getTemperature(),  0.1));
            entity.clearFire();
        }
    }
}
