package com.momosoftworks.coldsweat.api.event.core.registry;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.RegistryHolder;
import com.momosoftworks.coldsweat.data.codec.configuration.RemoveRegistryData;
import com.momosoftworks.coldsweat.data.RegistryHolder;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.eventbus.api.Event;

import javax.xml.ws.Holder;
import java.util.Collection;

/**
 * Gives subscribers unrestricted access to Cold Sweat's registries as they are being loaded.<br>
 * <br>
 * Fired on the Forge event bus when Cold Sweat's registries are gathered, but before they are committed to {@link ConfigSettings} where they become usable.<br>
 */
public abstract class LoadRegistriesEvent extends Event
{
    DynamicRegistries registryAccess;
    Multimap<RegistryKey<? extends Registry<? extends ConfigData>>, ? extends ConfigData> registries;

    public LoadRegistriesEvent(DynamicRegistries registryAccess, Multimap<RegistryKey<? extends Registry<? extends ConfigData>>, ? extends ConfigData> registries)
    {
        this.registryAccess = registryAccess;
        this.registries = registries;
    }

    public DynamicRegistries getRegistryAccess()
    {   return registryAccess;
    }

    public Multimap<RegistryKey<? extends Registry<? extends ConfigData>>, ? extends ConfigData> getRegistries()
    {   return registries;
    }

    public <T extends ConfigData> Collection<T> getRegistry(RegistryHolder<T> registry)
    {   return (Collection) registries.get(registry.key());
    }

    public <T extends ConfigData> void addRegistryEntry(RegistryHolder<T> key, T value)
    {   ((Multimap) registries).put(key.key(), value);
    }

    public <T extends ConfigData> void addRegistryEntries(RegistryHolder<T> registry, Collection<T> values)
    {   ((Multimap) registries).putAll(registry.key(), values);
    }

    /**
     * Fired directly after registries have been gathered, before registry removals are processed.<br>
     * <br>
     * Registry entries can be modified during this event, and they will be committed to Cold Sweat's runtime configs.
     */
    public static class Pre extends LoadRegistriesEvent
    {
        private Multimap<RegistryKey<Registry<? extends ConfigData>>, RemoveRegistryData<?>> removals;

        public Pre(DynamicRegistries registryAccess,
                   Multimap<RegistryKey<? extends Registry<? extends ConfigData>>, ? extends ConfigData> registries,
                   Multimap<RegistryKey<Registry<? extends ConfigData>>, RemoveRegistryData<?>> removals)
        {
            super(registryAccess, registries);
        }

        /**
         * @return An IMMUTABLE multimap of registry removals.
         */
        public Multimap<RegistryKey<Registry<? extends ConfigData>>, RemoveRegistryData<?>> getRegistryRemovals()
        {   return ImmutableMultimap.copyOf(removals);
        }
    }

    /**
     * Fired after Cold Sweat's registries have been gathered and committed to {@link ConfigSettings}.<br>
     * Registry removals have been processed at this point.<br>
     * <br>
     * <b>Use this event to commit your custom registries.</b>
     */
    public static class Post extends LoadRegistriesEvent
    {
        public Post(DynamicRegistries registryAccess, Multimap<RegistryKey<? extends Registry<? extends ConfigData>>, ? extends ConfigData> registries)
        {   super(registryAccess, registries);
        }
    }
}
