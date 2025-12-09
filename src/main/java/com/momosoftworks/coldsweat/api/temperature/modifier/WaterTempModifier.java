package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.tag.ModBiomeTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class WaterTempModifier extends TempModifier
{
    public WaterTempModifier()
    {   this(0);
    }

    public WaterTempModifier(double temperature)
    {   this.getNBT().putDouble("Temperature", temperature);
    }

    public double getTemperature()
    {   return this.getNBT().getDouble("Temperature");
    }

    public void setTemperature(double temperature)
    {
        this.getNBT().putDouble("Temperature", temperature);
        if (temperature != this.getTemperature())
        {   this.markDirty();
        }
    }

    public double getTargetTemperature(LivingEntity entity)
    {
        Double[] waterTemps = WorldHelper.getPositionGrid(entity.blockPosition(), 9, 4).stream()
                              .map(pos -> WorldHelper.getWaterTemperatureAt(entity.level(), pos))
                              .toArray(Double[]::new);
        return CSMath.average(waterTemps);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        double worldTemp = Temperature.get(entity, Temperature.Trait.WORLD);
        double minWorldTemp = ConfigSettings.MIN_TEMP.get();
        double maxWorldTemp = ConfigSettings.MAX_TEMP.get();
        double configDrySpeed = ConfigSettings.DRYOFF_SPEED.get();

        double temperature = this.getTemperature();
        double target = this.getTargetTemperature(entity);
        double addAmount;
        if (WorldHelper.isInWater(entity))
        {
            if (temperature < target)
            {   addAmount = Math.min(ConfigSettings.WATER_SOAK_SPEED.get(), target - temperature);
            }
            else
            {   addAmount = Math.max(-ConfigSettings.WATER_SOAK_SPEED.get(), target - temperature);
            }
        }
        else if (WorldHelper.isRainingAt(entity.level(), entity.blockPosition()))
        {   addAmount = Math.max(-ConfigSettings.RAIN_SOAK_SPEED.get(), -ConfigSettings.MAX_RAIN_SOAK.get() - temperature);
        }
        else
        {   addAmount = 0;
        }
        double dryAmount = WorldHelper.isInWater(entity) ? 0
                         : CSMath.blendExp(configDrySpeed / 1.5, configDrySpeed * 5, worldTemp, minWorldTemp, maxWorldTemp, 20);

        double tickRate = this.getTickRate() / 5.0;
        double newTemperature = CSMath.shrink(temperature + addAmount * tickRate, dryAmount * tickRate);
        if (newTemperature == 0)
        {   this.expires(0);
        }
        this.setTemperature(newTemperature);

        return temp -> temp + newTemperature;
    }

    @Override
    public void tick(LivingEntity entity)
    {
        if (entity.level().isClientSide() && ConfigSettings.WATER_EFFECT_SETTING.get().showParticles() && !entity.isInWater())
        {
            if (Math.random() < Math.abs(this.getTemperature()) * 2)
            {
                double randX = entity.getBbWidth() * (Math.random() - 0.5);
                double randY = entity.getBbHeight() * Math.random();
                double randZ = entity.getBbWidth() * (Math.random() - 0.5);
                entity.level().addParticle(ParticleTypes.FALLING_WATER, entity.getX() + randX, entity.getY() + randY, entity.getZ() + randZ, 0, 0, 0);
            }
        }
        if (!entity.level().isClientSide && entity.isOnFire())
        {
            this.setTemperature(CSMath.shrink(this.getTemperature(),  0.1));
            entity.clearFire();
        }
    }
}
