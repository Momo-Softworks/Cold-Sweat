package com.momosoftworks.coldsweat.api.event.core.registry;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.RegistryHolder;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

/**
 * <b>!! NOT CALLED IN THIS VERSION !!</b><br>
 * This event is not called in 1.18 because Forge loads registries extremely early in the mod loading process, long before events can be fired.<br>
 * <br>
 * Fired when Cold Sweat is building registries. <br>
 * Use this event to add your own registries to Cold Sweat's registry system.
 */
public class AddRegistriesEvent extends Event
{
    public AddRegistriesEvent()
    {}

    /**
     * Creates a Cold Sweat registry.
     */
    public <V extends ConfigData> RegistryHolder<V> createRegistry(ResourceLocation registry, Codec<V> codec, Class<V> type)
    {   return ModRegistries.createRegistry(registry, codec, type);
    }
}
