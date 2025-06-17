package com.momosoftworks.coldsweat.api.event.core.init;

import com.momosoftworks.coldsweat.data.tag.TagHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Predicate;

public class InitDynamicTagsEvent extends Event
{
    public final RegistryAccess registryAccess;

    public InitDynamicTagsEvent(RegistryAccess registryAccess)
    {   this.registryAccess = registryAccess;
    }

    public <T> void fillTag(TagKey<T> tag, Predicate<T> predicate, ResourceKey<Registry<T>> registry)
    {   TagHelper.fillTag(tag, predicate, registry, registryAccess);
    }
}
