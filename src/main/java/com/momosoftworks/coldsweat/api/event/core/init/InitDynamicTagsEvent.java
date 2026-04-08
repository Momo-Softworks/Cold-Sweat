package com.momosoftworks.coldsweat.api.event.core.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class InitDynamicTagsEvent<T> extends Event
{
    private final Registry<T> registry;
    private final Map<ResourceLocation, Collection<Holder<T>>> tags = new HashMap<>();

    public InitDynamicTagsEvent(Registry<T> registry)
    {   this.registry = registry;
    }

    public Map<ResourceLocation, Collection<Holder<T>>> getTags()
    {   return this.tags;
    }

    public void fillTag(TagKey<T> tag, Predicate<T> predicate)
    {
        if (this.registry == null || !tag.registry().equals(this.registry.key()))
        {   return;
        }
        this.registry.holders().forEach(holder ->
        {
            if (predicate.test(holder.value()))
            {    this.tags.computeIfAbsent(tag.location(), t -> new ArrayList<>()).add(holder);
            }
        });
    }
}
