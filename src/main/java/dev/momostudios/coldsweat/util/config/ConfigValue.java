package dev.momostudios.coldsweat.util.config;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Supplier;

/**
 * Contains a value that updates again once Forge has been fully loaded. Mostly used for static fields.
 * @param <T> The variable type that this object is storing
 */
public class ConfigValue<T>
{
    T value;
    Supplier<T> valueCreator;

    public ConfigValue(Supplier<T> valueCreator)
    {
        this.valueCreator = valueCreator;
        this.value = valueCreator.get();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static <V> ConfigValue<V> of(Supplier<V> valueCreator)
    {
        return new ConfigValue<>(valueCreator);
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
