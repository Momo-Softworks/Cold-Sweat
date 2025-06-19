package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.exceptions.SerializationException;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import java.util.function.*;

/**
 * Contains a value that updates as needed (usually when a player interacts with the config screen). <br>
 * If added to {@link ConfigSettings#CONFIG_SETTINGS}, it will be synced to the client.
 */
public class DynamicHolder<T> implements Supplier<T>
{
    private T value;
    private ResourceLocation name;
    private Supplier<T> valueCreator;
    private Loader<T> loader;
    private Saver<T> saver;
    private Codec<T> codec;
    private SyncType syncType = SyncType.NONE;
    private boolean requireRegistries = false;

    protected DynamicHolder(ResourceLocation name, Supplier<T> valueCreator, Consumer<DynamicHolder<T>> loader)
    {   this.name = name;
        this.valueCreator = valueCreator;
        this.loader = (holder, registryAccess) -> loader.accept(holder);
    }

    protected DynamicHolder(ResourceLocation name, Supplier<T> valueCreator, Loader<T> loader)
    {   this.name = name;
        this.valueCreator = valueCreator;
        this.loader = loader;
    }

    /**
     * Creates a simple value holder.
     * @param valueCreator A supplier that returns the holder's value.
     * @return A value holder.
     * @param <T> The type of the value.
     */
    public static <T> DynamicHolder<T> createWithRegistries(ResourceLocation name, Supplier<T> valueCreator, Loader<T> loader)
    {
        DynamicHolder<T> holder = new DynamicHolder<>(name, valueCreator, loader);
        holder.requireRegistries = true;
        return holder;
    }

    public static <T> DynamicHolder<T> create(ResourceLocation name, Supplier<T> valueCreator, Consumer<DynamicHolder<T>> loader)
    {   return new DynamicHolder<>(name, valueCreator, loader);
    }

    public static <T> DynamicHolder<T> create(ResourceLocation name, Supplier<T> valueCreator)
    {   return new DynamicHolder<>(name, valueCreator, holder -> {});
    }

    /**
     * Creates a value holder that can be synced between the server and client.
     * @param valueCreator A supplier that returns the holder's value.
     * @param codec handles network synchronization.
     * @param saver Writes the value to a config file. This may be either on the server or client.
     * @return A synced value holder.
     * @param <T> The type of the value.
     */
    public static <T> DynamicHolder<T> createSyncedWithRegistries(ResourceLocation name, Supplier<T> valueCreator, Loader<T> loader, Codec<T> codec, Saver<T> saver, SyncType syncType)
    {
        if (syncType == SyncType.NONE)
        {   throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("SyncType cannot be NONE for a synced DynamicHolder."));
        }
        DynamicHolder<T> holder = new DynamicHolder<>(name, valueCreator, loader);
        holder.codec = codec;
        holder.saver = saver;
        holder.syncType = syncType;
        holder.requireRegistries = true;
        return holder;
    }

    public static <T> DynamicHolder<T> createSynced(ResourceLocation name, Supplier<T> valueCreator, Consumer<DynamicHolder<T>> loader, Codec<T> codec, Consumer<T> saver, SyncType syncType)
    {
        if (syncType == SyncType.NONE)
        {   throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("SyncType cannot be NONE for a synced DynamicHolder."));
        }
        DynamicHolder<T> holder = new DynamicHolder<>(name, valueCreator, loader);
        holder.saver = (val, registryAccess) -> saver.accept(val);
        holder.codec = codec;
        holder.syncType = syncType;
        return holder;
    }

    @Override
    public T get()
    {
        if (this.requireRegistries)
        {   throw ColdSweat.LOGGER.throwing(new RuntimeException("RegistryAccess is required for this DynamicHolder, yet none was provided."));
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
        if (!isSynced())
        {  throw ColdSweat.LOGGER.throwing(SerializationException.serialize(this.value, "Tried to encode non-synced DynamicHolder", null));
        }
        try
        {
            CompoundTag compound = new CompoundTag();
            DataResult<Tag> holder = this.codec.encodeStart(NbtOps.INSTANCE, this.get(registryAccess));
            Tag encoded = holder.result().orElseThrow();
            compound.put(this.name.toString(), encoded);
            return compound;
        }
        catch (Exception e)
        {   throw ColdSweat.LOGGER.throwing(SerializationException.serialize(this.value, "Failed to encode DynamicHolder for type " + this.value.getClass().getSimpleName(), e));
        }
    }

    public void decode(CompoundTag tag, RegistryAccess registryAccess)
    {
        if (!isSynced())
        {  throw ColdSweat.LOGGER.throwing(new SerializationException("Tried to decode non-synced DynamicHolder"));
        }
        try
        {
            Tag encoded = tag.get(this.name.toString());
            if (encoded == null)
            {   throw ColdSweat.LOGGER.throwing(new SerializationException("No value found for DynamicHolder with name " + this.name));
            }
            this.value = this.codec.parse(NbtOps.INSTANCE, encoded).result().orElseThrow();
            this.saver.save(this.value, registryAccess);
        }
        catch (Exception e)
        {   throw ColdSweat.LOGGER.throwing(new SerializationException("Failed to decode DynamicHolder", e));
        }
    }

    public void save(RegistryAccess registryAccess)
    {
        if (!isSynced())
        {  throw ColdSweat.LOGGER.throwing(new SerializationException("Tried to save non-synced DynamicHolder"));
        }
        try
        {   saver.save(this.get(registryAccess), registryAccess);
        }
        catch (Exception e)
        {   throw ColdSweat.LOGGER.throwing(new SerializationException("Failed to save DynamicHolder for type " + this.value.getClass().getSimpleName(), e));
        }
    }

    public void reset()
    {   this.value = this.valueCreator.get();
    }

    public boolean isSynced()
    {   return syncType != SyncType.NONE;
    }

    public SyncType getSyncType()
    {   return syncType;
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
    public interface Saver<T>
    {
        void save(T value, RegistryAccess registryAccess);
    }

    public enum SyncType
    {
        // The value is not synced between the server and client.
        NONE,
        // The value is synced server -> client AND client -> server (when the config menu is used)
        BOTH_WAYS,
        // The value is synced server -> client only
        ONE_WAY;

        public boolean canSend()
        {
            return this == BOTH_WAYS || (this == ONE_WAY && EffectiveSide.get().isServer());
        }

        public boolean canReceive()
        {   return this == BOTH_WAYS || (this == ONE_WAY && EffectiveSide.get().isClient() && !Minecraft.getInstance().isLocalServer());
        }
    }
}
