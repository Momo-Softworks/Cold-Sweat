package com.momosoftworks.coldsweat.api.event.core.registry;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;

import java.util.*;

/**
 * Gives subscribers unrestricted access to Cold Sweat's registries as they are being loaded.<br>
 * <br>
 * Fired on the Forge event bus when Cold Sweat's registries are gathered, but before they are committed to {@link com.momosoftworks.coldsweat.config.ConfigSettings} where they become usable.<br>
 * <br>
 * This is not an {@link net.neoforged.bus.api.ICancellableEvent}.
 */
public abstract class CreateRegistriesEvent extends Event
{
    RegistryAccess registryAccess;
    Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries;

    public CreateRegistriesEvent(RegistryAccess registryAccess, Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries)
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

    public <T extends ConfigData> Collection<Holder<T>> getRegistry(ResourceKey<? extends Registry<T>> key)
    {   return (Collection) registries.get(key);
    }

    /**
     * Fired directly after registries have been gathered, before registry removals are processed.<br>
     * <br>
     * Registry entries can be modified during this event, and they will be committed to Cold Sweat's runtime configs.
     */
    public static class Pre extends CreateRegistriesEvent
    {
        private Multimap<ResourceKey<Registry<? extends ConfigData>>, RemoveRegistryData<?>> removals;

        public Pre(RegistryAccess registryAccess,
                   Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries,
                   Multimap<ResourceKey<Registry<? extends ConfigData>>, RemoveRegistryData<?>> removals)
        {
            super(registryAccess, registries);
        }

        /**
         * @return An IMMUTABLE multimap of registry removals.
         */
        public Multimap<ResourceKey<Registry<? extends ConfigData>>, RemoveRegistryData<?>> getRegistryRemovals()
        {   return ImmutableMultimap.copyOf(removals);
        }
    }

    /**
     * Fired after Cold Sweat's registries have been gathered and committed to {@link com.momosoftworks.coldsweat.config.ConfigSettings}.<br>
     * Registry removals have been processed at this point.<br>
     * <br>
     * <b>Use this event to commit your custom registries.</b>
     */
    public static class Post extends CreateRegistriesEvent
    {
        public Post(RegistryAccess registryAccess, Multimap<ResourceKey<? extends Registry<? extends ConfigData>>, Holder<? extends ConfigData>> registries)
        {   super(registryAccess, registries);
        }
    }
}
