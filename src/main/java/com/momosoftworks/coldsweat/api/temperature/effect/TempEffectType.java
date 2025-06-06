package com.momosoftworks.coldsweat.api.temperature.effect;

import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class TempEffectType<T extends TempEffect> extends ForgeRegistryEntry<TempEffectType<?>>
{
    private final Constructor<T> constructor;

    public TempEffectType(Constructor<T> constructor)
    {   this.constructor = constructor;
    }

    public T create(TempEffectType<T> type, LivingEntity entity, IntegerBounds range)
    {   return constructor.create(type, entity, range);
    }

    @FunctionalInterface
    public interface Constructor<T extends TempEffect>
    {   T create(TempEffectType<T>type, LivingEntity entity, IntegerBounds range);
    }
}