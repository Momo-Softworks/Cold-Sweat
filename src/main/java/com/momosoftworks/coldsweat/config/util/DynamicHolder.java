package com.momosoftworks.coldsweat.config.util;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Contains a value that updates as needed (usually when a player interacts with the config screen). <br>
 * If added to {@link ConfigSettings#CONFIG_SETTINGS}, it will be synced to the client.
 */
public class DynamicHolder<T>
{
    private T value;
    private final Supplier<T> valueCreator;
    private Function<T, CompoundTag> encoder;
    private Function<CompoundTag, T> decoder;
    private Consumer<T> saver;
    private boolean synced = false;

    public DynamicHolder(Supplier<T> valueCreator)
    {   this.valueCreator = valueCreator;
    }

    /**
     * Creates a simple value holder.
     * @param valueCreator A supplier that returns the holder's value.
     * @return A value holder.
     * @param <V> The type of the value.
     */
    public static <V> DynamicHolder<V> create(Supplier<V> valueCreator)
    {   return new DynamicHolder<>(valueCreator);
    }

    /**
     * Creates a value holder that can be synced between the server and client.
     * @param valueCreator A supplier that returns the holder's value.
     * @param encoder Writes the value to a CompoundTag.
     * @param decoder Reads the value from a CompoundTag.
     * @param saver Writes the value to a config file. This may be either on the server or client.
     * @return A synced value holder.
     * @param <V> The type of the value.
     */
    public static <V> DynamicHolder<V> createSynced(Supplier<V> valueCreator, Function<V, CompoundTag> encoder, Function<CompoundTag, V> decoder, Consumer<V> saver)
    {   DynamicHolder<V> loader = new DynamicHolder<>(valueCreator);
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
    {
        if (!this.value.getClass().isInstance(value))
        {   throw new RuntimeException(String.format("Cannot cast value of type %s to DynamicHolder of type %s", value.getClass(), this.value.getClass()));
        }
        this.value = (T) value;
    }

    public void load()
    {   this.value = valueCreator.get();
    }

    public CompoundTag encode()
    {   if (!synced)
        {  throw new RuntimeException("Tried to encode non-synced DynamicHolder for type " + this.value.getClass());
        }
        try
        {   return encoder.apply(this.get());
        }
        catch (Exception e)
        {   throw new RuntimeException("Failed to encode DynamicHolder for type " + this.value.getClass(), e);
        }
    }

    public void decode(CompoundTag tag)
    {   if (!synced)
        {  throw new RuntimeException("Tried to decode non-synced DynamicHolder for type " + this.value.getClass());
        }
        try
        {   this.value = decoder.apply(tag);
        }
        catch (Exception e)
        {   throw new RuntimeException("Failed to decode DynamicHolder for type " + this.value.getClass(), e);
        }
    }

    public void save()
    {   if (!synced)
        {  throw new RuntimeException("Tried to save non-synced DynamicHolder for type " + this.value.getClass());
        }
        try
        {   saver.accept(this.get());
        }
        catch (Exception e)
        {   throw new RuntimeException("Failed to save DynamicHolder for type " + this.value.getClass(), e);
        }
    }

    public boolean isSynced()
    {   return synced;
    }
}
