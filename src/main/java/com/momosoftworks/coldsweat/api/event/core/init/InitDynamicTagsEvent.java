package com.momosoftworks.coldsweat.api.event.core.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
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

    private static final Field MANAGER_ACCESS = ObfuscationReflectionHelper.findField(TagManager.class, "registryAccess");
    static { MANAGER_ACCESS.setAccessible(true); }

    public void fillTag(TagKey<T> tag, Predicate<T> predicate)
    {
        if (!tag.registry().equals(this.registry.key()))
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
