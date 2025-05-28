package com.momosoftworks.coldsweat.api.temperature.effect;

import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiFunction;

public class TempEffectType<T extends TempEffect>
{
    private final BiFunction<LivingEntity, IntegerBounds, T> constructor;

    public TempEffectType(BiFunction<LivingEntity, IntegerBounds, T> constructor)
    {   this.constructor = constructor;
    }

    public T create(LivingEntity entity, IntegerBounds range)
    {   return constructor.apply(entity, range);
    }
}