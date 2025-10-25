package com.momosoftworks.coldsweat.api.event.core.registry;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.momosoftworks.coldsweat.data.RegistryHolder;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.Event;

import java.util.*;

/**
 * Gives subscribers unrestricted access to Cold Sweat's registries as they are being loaded.<br>
 * <br>
 * Fired on the Forge event bus when Cold Sweat's registries are gathered, but before they are committed to {@link com.momosoftworks.coldsweat.config.ConfigSettings} where they become usable.<br>
 */
public abstract class LoadRegistriesEvent extends Event
{
    RegistryAccess registryAccess;
    Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries;

    public LoadRegistriesEvent(RegistryAccess registryAccess, Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries)
    {
        this.registryAccess = registryAccess;
        this.registries = registries;
    }

    public RegistryAccess getRegistryAccess()
    {   return registryAccess;
    }

    public Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> getRegistries()
    {   return registries;
    }

    public <T extends ConfigData> Collection<Holder<T>> getRegistry(RegistryHolder<T> registry)
    {   return (Collection) registries.get(registry.key());
    }

    public <T extends ConfigData> void addRegistryEntry(RegistryHolder<T> key, Holder<T> value)
    {   registries.put(key.key(), value);
    }

    public <T extends ConfigData> void addRegistryEntry(RegistryHolder<T> key, T value)
    {   registries.put(key.key(), Holder.direct(value));
    }

    public <T extends ConfigData> void addRegistryEntries(RegistryHolder<T> key, Collection<Holder<T>> values)
    {   registries.putAll(key.key(), values);
    }

    /**
     * Fired directly after registries have been gathered, before registry removals are processed.<br>
     * <br>
     * Registry entries can be modified during this event, and they will be committed to Cold Sweat's runtime configs.
     */
    public static class Pre extends LoadRegistriesEvent
    {
        private Multimap<ResourceKey<Registry<? extends ConfigData>>, RegistryModifierData<?>> modifiers;

        public Pre(RegistryAccess registryAccess,
                   Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries,
                   Multimap<ResourceKey<Registry<? extends ConfigData>>, RegistryModifierData<?>> modifiers)
        {
            super(registryAccess, registries);
        }

        /**
         * @return An IMMUTABLE multimap of registry modifiers.
         */
        public Multimap<ResourceKey<Registry<? extends ConfigData>>, RegistryModifierData<?>> getRegistryModifiers()
        {   return ImmutableMultimap.copyOf(modifiers);
        }
    }

    /**
     * Fired after Cold Sweat's registries have been gathered and committed to {@link com.momosoftworks.coldsweat.config.ConfigSettings}.<br>
     * Registry removals have been processed at this point.<br>
     * <br>
     * <b>Use this event to commit your custom registries.</b>
     */
    public static class Post extends LoadRegistriesEvent
    {
        public Post(RegistryAccess registryAccess, Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries)
        {   super(registryAccess, registries);
        }
    }
}
