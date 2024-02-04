package com.momosoftworks.coldsweat.config.util;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Contains a value that updates as needed (usually when a player interacts with the config screen). <br>
 * If added to {@link ConfigSettings#CONFIG_SETTINGS}, it will be synced to the client.
 */
public class ValueHolder<T>
{
    private T value;
    private final Supplier<T> valueCreator;
    private Function<T, CompoundNBT> encoder;
    private Function<CompoundNBT, T> decoder;
    private Consumer<T> saver;
    private boolean synced = false;

    public ValueHolder(Supplier<T> valueCreator)
    {   this.valueCreator = valueCreator;
    }

    /**
     * Creates a simple value holder.
     * @param valueCreator A supplier that returns the holder's value.
     * @return A value holder.
     * @param <V> The type of the value.
     */
    public static <V> ValueHolder<V> simple(Supplier<V> valueCreator)
    {   return new ValueHolder<>(valueCreator);
    }

    /**
     * Creates a value holder that can be synced between the server and client.
     * @param valueCreator A supplier that returns the holder's value.
     * @param encoder Writes the value to a CompoundNBT.
     * @param decoder Reads the value from a CompoundNBT.
     * @param saver Writes the value to a config file. This may be either on the server or client.
     * @return A synced value holder.
     * @param <V> The type of the value.
     */
    public static <V> ValueHolder<V> synced(Supplier<V> valueCreator, Function<V, CompoundNBT> encoder, Function<CompoundNBT, V> decoder, Consumer<V> saver)
    {
        ValueHolder<V> loader = new ValueHolder<>(valueCreator);
        loader.encoder = encoder;
        loader.decoder = decoder;
        loader.saver = saver;
        loader.synced = true;
        return loader;
    }

    public T get()
    {
        if (this.value == null)
        {   this.load();
        }
        return value;
    }

    public void set(Object value)
    {   try
        {   this.value = (T) value;
        }
        catch (Exception e) { throw new RuntimeException("Failed to set value of type " + value.getClass() + " to " + value); }
    }

    public void load()
    {   this.value = valueCreator.get();
    }

    public CompoundNBT encode()
    {   return encoder.apply(value);
    }

    public void decode(CompoundNBT tag)
    {   this.value = decoder.apply(tag);
    }

    public void save()
    {   saver.accept(value);
    }

    public boolean isSynced()
    {   return synced;
    }
}
