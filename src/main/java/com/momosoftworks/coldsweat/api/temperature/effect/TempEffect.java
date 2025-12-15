package com.momosoftworks.coldsweat.api.temperature.effect;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public abstract class TempEffect
{
    private final LivingEntity entity;
    private final IntegerBounds bounds;
    private final TempEffectType<?> type;

    public TempEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds range)
    {
        this.type = type;
        this.entity = entity;
        this.bounds = range;
    }

    public abstract Side getSide();

    public TempEffectType<?> type()
    {   return this.type;
    }
    protected LivingEntity entity()
    {   return this.entity;
    }
    protected IntegerBounds bounds()
    {   return this.bounds;
    }

    protected boolean test(Entity entity)
    {
        return Objects.equals(this.entity, entity)
            && !entity.isSpectator()
            && !(entity instanceof Player player && player.isCreative())
            && this.bounds().test(Math.round((float) CSMath.clamp(this.getTemperature(), -100, 100)));
    }

    protected double getTemperature()
    {   return this.entity.level().isClientSide ? Overlays.BLEND_BODY_TEMP : Temperature.get(entity, Temperature.Trait.BODY);
    }

    public double getEffectFactor()
    {
        if (EntityTempManager.isImmuneToTemperature(this.entity)) return 0;
        double temperature = this.getTemperature();
        double minTemp = this.bounds().min();
        double maxTemp = this.bounds().max();
        double resistance = EntityTempManager.getResistance(temperature, this.entity);
        temperature = CSMath.blend(temperature, minTemp, resistance, 0, 1);
        return CSMath.blend(0, 1, temperature, minTemp, maxTemp);
    }

    public enum Side
    {
        CLIENT,
        SERVER,
        COMMON;

        public boolean checkSide(boolean isClient)
        {   return (this == CLIENT && isClient) || (this == SERVER && !isClient) || this == COMMON;
        }
    }
}