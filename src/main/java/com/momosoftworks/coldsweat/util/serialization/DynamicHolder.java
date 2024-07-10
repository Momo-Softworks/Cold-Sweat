package com.momosoftworks.coldsweat.util.serialization;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.nbt.CompoundNBT;
import com.momosoftworks.coldsweat.util.exceptions.SerializationException;
import net.minecraft.util.registry.DynamicRegistries;

import java.util.function.*;

/**
 * Contains a value that updates as needed (usually when a player interacts with the config screen). <br>
 * If added to {@link ConfigSettings#CONFIG_SETTINGS}, it will be synced to the client.
 */
public class DynamicHolder<T>
{
    private T value;
    private final Getter<T> valueCreator;
    private Writer<T> encoder;
    private Reader<T> decoder;
    private Saver<T> saver;
    private boolean synced = false;
    private boolean requireRegistries = false;

    protected DynamicHolder(Supplier<T> valueCreator)
    {   this.valueCreator = (registryAccess) -> valueCreator.get();
    }

    protected DynamicHolder(Getter<T> valueCreator)
    {   this.valueCreator = valueCreator;
    }

    /**
     * Creates a simple value holder.
     * @param valueCreator A supplier that returns the holder's value.
     * @return A value holder.
     * @param <T> The type of the value.
     */
    public static <T> DynamicHolder<T> createWithRegistries(Getter<T> valueCreator)
    {
        DynamicHolder<T> loader = new DynamicHolder<>(valueCreator);
        loader.requireRegistries = true;
        return loader;
    }

    public static <T> DynamicHolder<T> create(Supplier<T> valueCreator)
    {   return new DynamicHolder<>(valueCreator);
    }

    /**
     * Creates a value holder that can be synced between the server and client.
     * @param valueCreator A supplier that returns the holder's value.
     * @param encoder Writes the value to a CompoundNBT.
     * @param decoder Reads the value from a CompoundNBT.
     * @param saver Writes the value to a config file. This may be either on the server or client.
     * @return A synced value holder.
     * @param <T> The type of the value.
     */
    public static <T> DynamicHolder<T> createSyncedWithRegistries(Getter<T> valueCreator, Writer<T> encoder, Reader<T> decoder, Saver<T> saver)
    {
        DynamicHolder<T> loader = new DynamicHolder<>(valueCreator);
        loader.encoder = encoder;
        loader.decoder = decoder;
        loader.saver = saver;
        loader.synced = true;
        loader.requireRegistries = true;
        return loader;
    }

    public static <T> DynamicHolder<T> createSynced(Supplier<T> valueCreator, Function<T, CompoundNBT> encoder, Function<CompoundNBT, T> decoder, Consumer<T> saver)
    {
        DynamicHolder<T> loader = new DynamicHolder<>(valueCreator);
        loader.encoder = (val, registryAccess) -> encoder.apply(val);
        loader.decoder = (tag, registryAccess) -> decoder.apply(tag);
        loader.saver = (val, registryAccess) -> saver.accept(val);
        loader.synced = true;
        return loader;
    }

    public T get()
    {
        if (this.requireRegistries)
        {
            throw new RuntimeException("DynamicRegistries is required for this DynamicHolder, yet none was provided.");
        }
        if (this.value == null)
        {   this.load();
        }
        return value;
    }

    public T get(DynamicRegistries registryAccess)
    {
        if (this.value == null)
        {   this.load(registryAccess);
        }
        return value;
    }

    public void set(Object value)
    {
        if (!this.value.getClass().isInstance(value))
        {   throw new ClassCastException(String.format("Cannot cast value of type %s to DynamicHolder of type %s", value.getClass(), this.value.getClass()));
        }
        this.value = (T) value;
    }

    public void load()
    {   this.value = valueCreator.get(null);
    }

    public void load(DynamicRegistries registryAccess)
    {   this.value = valueCreator.get(registryAccess);
    }

    public CompoundNBT encode(DynamicRegistries registryAccess)
    {
        if (!synced)
        {  throw ColdSweat.LOGGER.throwing(SerializationException.serialize(this.value, "Tried to encode non-synced DynamicHolder", null));
        }
        try
        {   return encoder.write(this.get(), registryAccess);
        }
        catch (Exception e)
        {   throw ColdSweat.LOGGER.throwing(SerializationException.serialize(this.value, "Failed to encode DynamicHolder for type " + this.value.getClass().getSimpleName(), e));
        }
    }

    public void decode(CompoundNBT tag, DynamicRegistries registryAccess)
    {
        if (!synced)
        {  throw ColdSweat.LOGGER.throwing(new SerializationException("Tried to decode non-synced DynamicHolder", null));
        }
        try
        {   this.value = decoder.read(tag, registryAccess);
        }
        catch (Exception e)
        {   throw ColdSweat.LOGGER.throwing(new SerializationException("Failed to decode DynamicHolder for type " + this.value.getClass().getSimpleName(), e));
        }
    }

    public void save(DynamicRegistries registryAccess)
    {
        if (!synced)
        {  throw ColdSweat.LOGGER.throwing(new SerializationException("Tried to save non-synced DynamicHolder", null));
        }
        try
        {   saver.save(this.get(registryAccess), registryAccess);
        }
        catch (Exception e)
        {   throw ColdSweat.LOGGER.throwing(new SerializationException("Failed to save DynamicHolder for type " + this.value.getClass().getSimpleName(), e));
        }
    }

    public boolean isSynced()
    {   return synced;
    }

    public boolean requiresRegistries()
    {   return requireRegistries;
    }

    @FunctionalInterface
    public interface Getter<T>
    {
        T get(DynamicRegistries registryAccess);
    }

    @FunctionalInterface
    public interface Writer<T>
    {
        CompoundNBT write(T value, DynamicRegistries registryAccess);
    }

    @FunctionalInterface
    public interface Reader<T>
    {
        T read(CompoundNBT tag, DynamicRegistries registryAccess);
    }

    @FunctionalInterface
    public interface Saver<T>
    {
        void save(T value, DynamicRegistries registryAccess);
    }
}
