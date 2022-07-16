package dev.momostudios.coldsweat.util.config;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Supplier;

/**
 * Contains a value that updates again once Forge has been fully loaded. Mostly used for static fields.
 * @param <T> The variable type that this object is storing
 */
public class LoadedValue<T>
{
    T value;
    Supplier<T> valueCreator;

    public LoadedValue(Supplier<T> valueCreator)
    {
        this.valueCreator = valueCreator;
        this.value = valueCreator.get();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static <V> LoadedValue<V> of(Supplier<V> valueCreator)
    {
        return new LoadedValue<>(valueCreator);
    }

    @SubscribeEvent
    public void onLoaded(ServerStartedEvent event)
    {
        this.value = valueCreator.get();
    }

    public T get()
    {
        return value;
    }
}
