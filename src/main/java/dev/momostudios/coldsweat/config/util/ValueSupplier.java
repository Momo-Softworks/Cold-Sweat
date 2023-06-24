package dev.momostudios.coldsweat.config.util;

import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Contains a value that updates again once Forge has been fully loaded. Mostly used for static fields.
 * @param <T> The variable type that this object is storing
 */
public class ValueSupplier<T>
{
    private T value;
    private final Supplier<T> valueCreator;
    private Function<T, CompoundTag> encoder;
    private Function<CompoundTag, T> decoder;
    private Consumer<T> saver;
    private boolean synced = false;

    public ValueSupplier(Supplier<T> valueCreator)
    {
        this.valueCreator = valueCreator;
    }

    public static <V> ValueSupplier<V> of(Supplier<V> valueCreator)
    {
        return new ValueSupplier<>(valueCreator);
    }

    public static <V> ValueSupplier<V> synced(Supplier<V> valueCreator, Function<V, CompoundTag> encoder, Function<CompoundTag, V> decoder, Consumer<V> saver)
    {
        ValueSupplier<V> loader = new ValueSupplier<>(valueCreator);
        loader.encoder = encoder;
        loader.decoder = decoder;
        loader.saver = saver;
        loader.synced = true;
        return loader;
    }

    public T get()
    {
        if (this.value == null)
            load();
        return value;
    }

    public void set(T value)
    {   this.value = value;
    }

    public void load()
    {   this.value = valueCreator.get();
    }

    public CompoundTag encode()
    {   return encoder.apply(value);
    }

    public void decode(CompoundTag tag)
    {   this.value = decoder.apply(tag);
    }

    public void save()
    {   saver.accept(value);
    }

    public boolean isSynced()
    {   return synced;
    }
}
