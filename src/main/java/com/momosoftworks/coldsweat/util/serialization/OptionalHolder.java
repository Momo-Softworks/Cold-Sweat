package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.data.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public class OptionalHolder<T>
{
    private final ResourceKey<T> key;
    private Holder<T> value = null;

    public OptionalHolder(ResourceKey<T> key)
    {   this.key = key;
        ModRegistries.OPTIONAL_HOLDERS.add(this);
    }

    public static <T> Codec<OptionalHolder<T>> getCodec(ResourceKey<Registry<T>> key)
    {   return ResourceKey.codec(key).xmap(OptionalHolder::new, OptionalHolder::key);
    }

    public ResourceKey<T> key()
    {   return key;
    }

    public Holder<T> get()
    {   return Optional.ofNullable(value).orElseThrow();
    }

    public Optional<Holder<T>> value()
    {   return Optional.ofNullable(value);
    }

    public void setValue(Holder<T> value)
    {   this.value = value;
    }

    public static <T> OptionalHolder<T> ofHolder(Holder<T> holder)
    {
        OptionalHolder<T> opt = new OptionalHolder<>(holder.unwrapKey().orElseThrow());
        opt.setValue(holder);
        return opt;
    }

    public boolean is(Holder<T> holder)
    {   return value != null && value.equals(holder);
    }
}
