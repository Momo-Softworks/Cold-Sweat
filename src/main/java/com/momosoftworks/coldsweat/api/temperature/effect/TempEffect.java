package com.momosoftworks.coldsweat.api.temperature.effect;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

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

    public abstract boolean isClient();

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
    {   return Objects.equals(this.entity, entity) && this.bounds().test((int) CSMath.clamp(this.getTemperature(), -100, 100));
    }

    protected double getTemperature()
    {   return Temperature.get(entity, Temperature.Trait.BODY);
    }
}