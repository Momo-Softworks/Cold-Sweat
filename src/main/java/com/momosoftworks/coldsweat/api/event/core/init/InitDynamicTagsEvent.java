package com.momosoftworks.coldsweat.api.event.core.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class InitDynamicTagsEvent extends Event
{
    private final RegistryAccess registryAccess;
    private Map<TagKey<?>, Collection<Holder<?>>> tags = new HashMap<>();

    public InitDynamicTagsEvent(RegistryAccess registryAccess)
    {   this.registryAccess = registryAccess;
    }

    public Map<TagKey<?>, Collection<Holder<?>>> getTags()
    {   return this.tags;
    }

    private static final Field MANAGER_ACCESS = ObfuscationReflectionHelper.findField(TagManager.class, "f_144569_");
    static { MANAGER_ACCESS.setAccessible(true); }

    public <T> void fillTag(TagKey<T> tag, Predicate<T> predicate)
    {
        Registry<T> registry = registryAccess.registryOrThrow(tag.registry());
        registry.holders().forEach(holder ->
        {
            if (predicate.test(holder.value()))
            {    this.tags.computeIfAbsent(tag, t -> new ArrayList<>()).add(holder);
            }
        });
    }
}
