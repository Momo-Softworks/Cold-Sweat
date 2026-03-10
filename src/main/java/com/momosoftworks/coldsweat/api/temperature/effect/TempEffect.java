package com.momosoftworks.coldsweat.api.temperature.effect;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public abstract class TempEffect
{
    private final IntegerBounds bounds;
    private final TempEffectType<?> type;

    public TempEffect(TempEffectType<?> type, IntegerBounds range)
    {
        this.type = type;
        this.bounds = range;
    }

    public abstract Side getSide();

    public TempEffectType<?> type()
    {   return this.type;
    }
    protected IntegerBounds bounds()
    {   return this.bounds;
    }

    protected boolean test(LivingEntity entity)
    {
        return entity != null
            && !entity.isSpectator()
            && !(entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative())
            && this.bounds().test(Math.round((float) CSMath.clamp(this.getTemperature(entity), -100, 100)));
    }

    protected double getTemperature(LivingEntity entity)
    {   return entity.level.isClientSide ? Overlays.BLEND_BODY_TEMP : Temperature.get(entity, Temperature.Trait.BODY);
    }

    public double getEffectFactor(LivingEntity entity)
    {
        if (EntityTempManager.isImmuneToTemperature(entity)) return 0;
        double temperature = this.getTemperature(entity);
        double minTemp = this.bounds().min();
        double maxTemp = this.bounds().max();
        double resistance = EntityTempManager.getResistance(temperature, entity);
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