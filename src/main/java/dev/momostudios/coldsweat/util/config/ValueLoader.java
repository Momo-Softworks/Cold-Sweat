package dev.momostudios.coldsweat.util.config;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Supplier;

/**
 * Contains a value that updates again once Forge has been fully loaded. Mostly used for static fields.
 * @param <T> The variable type that this object is storing
 */
public class ValueLoader<T>
{
    T value;
    Supplier<T> valueCreator;

    public ValueLoader(Supplier<T> valueCreator)
    {
        this.valueCreator = valueCreator;
        this.value = valueCreator.get();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static <V> ValueLoader<V> of(Supplier<V> valueCreator)
    {
        return new ValueLoader<>(valueCreator);
    }

    @SubscribeEvent
    public void onLoaded(ServerStartedEvent event)
    {
        this.load();
    }

    public T get()
    {
        return value;
    }

    public void load()
    {
        this.value = valueCreator.get();
    }
}
