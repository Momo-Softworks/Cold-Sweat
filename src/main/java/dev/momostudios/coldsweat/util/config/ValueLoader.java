package dev.momostudios.coldsweat.util.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Contains a value that updates again once Forge has been fully loaded. Mostly used for static fields.
 * @param <T> The variable type that this object is storing
 */
public class ValueLoader<T>
{
    private T value;
    private final Supplier<T> valueCreator;
    private Function<T, CompoundTag> encoder;
    private Function<CompoundTag, T> decoder;
    private Consumer<T> saver;
    private boolean synced = false;

    public ValueLoader(Supplier<T> valueCreator)
    {
        this.valueCreator = valueCreator;
        this.value = valueCreator.get();
    }

    public static <V> ValueLoader<V> of(Supplier<V> valueCreator)
    {
        return new ValueLoader<>(valueCreator);
    }

    public static <V> ValueLoader<V> synced(Supplier<V> valueCreator, Function<V, CompoundTag> encoder, Function<CompoundTag, V> decoder, Consumer<V> saver)
    {
        ValueLoader<V> loader = new ValueLoader<>(valueCreator);
        loader.encoder = encoder;
        loader.decoder = decoder;
        loader.saver = saver;
        loader.synced = true;
        return loader;
    }

    public T get()
    {   return value;
    }

    public void set(T value)
    {   this.value = value;
    }

    public void reload()
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
