package com.momosoftworks.coldsweat.util.serialization;

import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;

import java.util.Objects;
import java.util.Optional;

public class OptionalHolder<T>
{
    private final ResourceKey<T> key;
    private Holder<T> value = null;

    public OptionalHolder(ResourceKey<T> key)
    {   this.key = key;
        ConfigLoadingHandler.addOptionalHolder(this);
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
    {   return Objects.equals(this.value, holder);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof OptionalHolder)) return false;
        OptionalHolder<?> that = (OptionalHolder<?>) obj;
        return Objects.equals(this.key, that.key)
            && Objects.equals(this.value, that.value);
    }

    @Override
    public String toString()
    {
        return "OptionalHolder{" +
                "key=" + key +
                ", value=" + (value != null ? value.value() : null) +
                '}';
    }
}
