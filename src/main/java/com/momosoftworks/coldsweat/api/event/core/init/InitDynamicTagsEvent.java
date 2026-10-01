package com.momosoftworks.coldsweat.api.event.core.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.function.Predicate;

public class InitDynamicTagsEvent<T> extends Event
{
    private final Registry<T> registry;
    private final Map<Identifier, Collection<Holder<T>>> tags = new HashMap<>();

    public InitDynamicTagsEvent(Registry<T> registry)
    {   this.registry = registry;
    }

    public Map<Identifier, Collection<Holder<T>>> getTags()
    {   return this.tags;
    }

    public void fillTag(TagKey<T> tag, Predicate<T> predicate)
    {
        if (this.registry == null || !tag.registry().equals(this.registry.key()))
        {   return;
        }
        this.registry.listElements().forEach(holder ->
        {
            if (predicate.test(holder.value()))
            {    this.tags.computeIfAbsent(tag.location(), t -> new ArrayList<>()).add(holder);
            }
        });
    }

    /**
     * Fires this event for the given registry and merges the resulting dynamic tags into the loaded tags.<br>
     * Dynamic tags replace the contents of any loaded tag with the same ID.
     */
    public static <T> Map<Identifier, List<Holder<T>>> applyDynamicTags(Registry<T> registry, Map<Identifier, List<Holder<T>>> loadedTags)
    {
        InitDynamicTagsEvent<T> event = new InitDynamicTagsEvent<>(registry);
        NeoForge.EVENT_BUS.post(event);
        if (event.getTags().isEmpty())
        {   return loadedTags;
        }
        Map<Identifier, List<Holder<T>>> merged = new HashMap<>(loadedTags);
        event.getTags().forEach((id, values) ->
        {
            if (!values.isEmpty())
            {   merged.put(id, List.copyOf(values));
            }
        });
        return merged;
    }

    public static <T> Map<TagKey<T>, List<Holder<T>>> applyDynamicTagKeys(Registry<T> registry, Map<TagKey<T>, List<Holder<T>>> loadedTags)
    {
        ResourceKey<? extends Registry<T>> registryKey = registry.key();
        Map<Identifier, List<Holder<T>>> byId = new HashMap<>();
        loadedTags.forEach((key, values) -> byId.put(key.location(), values));
        Map<Identifier, List<Holder<T>>> merged = applyDynamicTags(registry, byId);
        if (merged == byId)
        {   return loadedTags;
        }
        Map<TagKey<T>, List<Holder<T>>> result = new HashMap<>();
        merged.forEach((id, values) -> result.put(TagKey.create(registryKey, id), values));
        return result;
    }
}
