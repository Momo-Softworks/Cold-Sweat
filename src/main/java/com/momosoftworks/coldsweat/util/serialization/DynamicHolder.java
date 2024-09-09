package com.momosoftworks.coldsweat.util.serialization;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.exceptions.SerializationException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;

import java.util.function.*;

/**
 * Contains a value that updates as needed (usually when a player interacts with the config screen). <br>
 * If added to {@link ConfigSettings#CONFIG_SETTINGS}, it will be synced to the client.
 */
public class DynamicHolder<T>
{
    private T value;
    private Supplier<T> valueCreator;
    private Loader<T> loader;
    private Saver<T> saver;
    private Writer<T> encoder;
    private Reader<T> decoder;
    private boolean synced = false;
    private boolean requireRegistries = false;

    protected DynamicHolder(Supplier<T> valueCreator, Consumer<DynamicHolder<T>> loader)
    {   this.valueCreator = valueCreator;
        this.loader = (holder, registryAccess) -> loader.accept(holder);
    }

    protected DynamicHolder(Supplier<T> valueCreator, Loader<T> loader)
    {   this.valueCreator = valueCreator;
        this.loader = loader;
    }

    /**
     * Creates a simple value holder.
     * @param valueCreator A supplier that returns the holder's value.
     * @return A value holder.
     * @param <T> The type of the value.
     */
    public static <T> DynamicHolder<T> createWithRegistries(Supplier<T> valueCreator, Loader<T> loader)
    {
        DynamicHolder<T> holder = new DynamicHolder<>(valueCreator, loader);
        holder.value = valueCreator.get();
        holder.requireRegistries = true;
        return holder;
    }

    public static <T> DynamicHolder<T> create(Supplier<T> valueCreator, Consumer<DynamicHolder<T>> loader)
    {
        DynamicHolder<T> holder = new DynamicHolder<>(valueCreator, loader);
        holder.value = valueCreator.get();
        return holder;
    }

    /**
     * Creates a value holder that can be synced between the server and client.
     * @param valueCreator A supplier that returns the holder's value.
     * @param encoder Writes the value to a CompoundTag.
     * @param decoder Reads the value from a CompoundTag.
     * @param saver Writes the value to a config file. This may be either on the server or client.
     * @return A synced value holder.
     * @param <T> The type of the value.
     */
    public static <T> DynamicHolder<T> createSyncedWithRegistries(Supplier<T> valueCreator, Loader<T> loader, Writer<T> encoder, Reader<T> decoder, Saver<T> saver)
    {
        DynamicHolder<T> holder = new DynamicHolder<>(valueCreator, loader);
        holder.value = valueCreator.get();
        holder.encoder = encoder;
        holder.decoder = decoder;
        holder.saver = saver;
        holder.synced = true;
        holder.requireRegistries = true;
        return holder;
    }

    public static <T> DynamicHolder<T> createSynced(Supplier<T> valueCreator, Consumer<DynamicHolder<T>> loader, Function<T, CompoundTag> encoder, Function<CompoundTag, T> decoder, Consumer<T> saver)
    {
        DynamicHolder<T> holder = new DynamicHolder<>(valueCreator, loader);
        holder.value = valueCreator.get();
        holder.encoder = (val, registryAccess) -> encoder.apply(val);
        holder.decoder = (tag, registryAccess) -> decoder.apply(tag);
        holder.saver = (val, registryAccess) -> saver.accept(val);
        holder.synced = true;
        return holder;
    }

    public T get()
    {
        if (this.requireRegistries)
        {
            throw new RuntimeException("RegistryAccess is required for this DynamicHolder, yet none was provided.");
        }
        if (this.value == null)
        {   this.load(true);
        }
        return value;
    }

    public T get(RegistryAccess registryAccess)
    {
        if (this.value == null)
        {   this.load(registryAccess, true);
        }
        return value;
    }

    public void set(T value)
    {
        this.value = value;
    }

    public void setUnsafe(Object value)
    {
        this.value = (T) value;
    }

    public void load(boolean replace)
    {
        if (replace)
        {   this.value = this.valueCreator.get();
        }
        this.loader.load(this, null);
    }

    public void load(RegistryAccess registryAccess, boolean replace)
    {
        if (replace)
        {   this.value = this.valueCreator.get();
        }
        this.loader.load(this, registryAccess);
    }

    public CompoundTag encode(RegistryAccess registryAccess)
    {
        if (!synced)
        {  throw ColdSweat.LOGGER.throwing(SerializationException.serialize(this.value, "Tried to encode non-synced DynamicHolder", null));
        }
        try
        {   return encoder.write(this.get(registryAccess), registryAccess);
        }
        catch (Exception e)
        {   throw ColdSweat.LOGGER.throwing(SerializationException.serialize(this.value, "Failed to encode DynamicHolder for type " + this.value.getClass().getSimpleName(), e));
        }
    }

    public void decode(CompoundTag tag, RegistryAccess registryAccess)
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

    public void save(RegistryAccess registryAccess)
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
    public interface Loader<T>
    {
        void load(DynamicHolder<T> holder, RegistryAccess registryAccess);
    }

    @FunctionalInterface
    public interface Writer<T>
    {
        CompoundTag write(T value, RegistryAccess registryAccess);
    }

    @FunctionalInterface
    public interface Reader<T>
    {
        T read(CompoundTag tag, RegistryAccess registryAccess);
    }

    @FunctionalInterface
    public interface Saver<T>
    {
        void save(T value, RegistryAccess registryAccess);
    }
}
