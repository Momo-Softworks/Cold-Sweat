package com.momosoftworks.coldsweat.api.temperature.effect;

import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.world.entity.LivingEntity;

public class TempEffectType<T extends TempEffect>
{
    private final Constructor<T> constructor;

    public TempEffectType(Constructor<T> constructor)
    {   this.constructor = constructor;
    }

    public T create(TempEffectType<T> type, IntegerBounds range)
    {   return constructor.create(type, range);
    }

    @FunctionalInterface
    public interface Constructor<T extends TempEffect>
    {   T create(TempEffectType<T>type, IntegerBounds range);
    }
}