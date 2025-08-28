package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.tag.ModBiomeTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Function;

public class WaterTempModifier extends TempModifier
{
    private static final double WATER_SOAK_SPEED = 0.1;
    private static final double RAIN_SOAK_SPEED = 0.0125;
    private static final double DRY_SPEED = 0.0015;

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
        boolean isWarm = this.getTemperature() >= 0 && entity.level.getBiome(entity.blockPosition()).is(ModBiomeTags.HAS_HOT_WATER);
        double worldTemp = Temperature.get(entity, Temperature.Trait.WORLD);
        double minWorldTemp = ConfigSettings.MIN_TEMP.get();
        double maxWorldTemp = ConfigSettings.MAX_TEMP.get();
        double configDrySpeed = ConfigSettings.DRYOFF_SPEED.get() * DRY_SPEED;

        double temperature = this.getTemperature();
        double addAmount = WorldHelper.isInWater(entity) ? WATER_SOAK_SPEED // In water
                         : WorldHelper.isRainingAt(entity.level, entity.blockPosition()) ? RAIN_SOAK_SPEED // In rain
                         : 0;
        double dryAmount = CSMath.blendExp(configDrySpeed, configDrySpeed * 10, worldTemp, minWorldTemp, maxWorldTemp, 100);
        double maxTemp = this.getMaxTemperature(entity);

        // Expire if effect is nullified
        if (CSMath.sign(temperature + dryAmount) != CSMath.sign(temperature))
        {   this.expires(0);
        }

        double newTemperature = isWarm ? Math.min(temperature + addAmount - dryAmount, maxTemp)
                                       : Math.max(temperature - addAmount + dryAmount, -maxTemp);
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
