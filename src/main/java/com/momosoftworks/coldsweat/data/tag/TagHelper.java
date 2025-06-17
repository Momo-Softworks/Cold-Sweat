package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.api.event.core.init.InitDynamicTagsEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ServerConfigsLoadedEvent;
import com.momosoftworks.coldsweat.util.serialization.ListBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

@EventBusSubscriber
public class TagHelper
{
    private static final Field CONTENTS = ObfuscationReflectionHelper.findField(HolderSet.Named.class, "contents");
    private static final Method BIND = ObfuscationReflectionHelper.findMethod(HolderSet.Named.class, "bind", List.class);
    private static final Method BIND_TAGS = ObfuscationReflectionHelper.findMethod(Holder.Reference.class, "bindTags", Collection.class);
    static
    {   CONTENTS.setAccessible(true);
        BIND.setAccessible(true);
        BIND_TAGS.setAccessible(true);
    }

    public static <T> void fillTag(TagKey<T> tag, Predicate<T> predicate, ResourceKey<Registry<T>> registry,  RegistryAccess registryAccess)
    {
        Registry<T> reg = registryAccess.registryOrThrow(registry);
        HolderSet.Named<T> holderSet = reg.getTag(tag).get();
        Set<Holder<T>> entries;
        try
        {   entries = new HashSet<>((List<Holder<T>>) CONTENTS.get(holderSet));
        }
        catch (IllegalAccessException e)
        {   throw new RuntimeException(e);
        }
        reg.holders().forEach(dimensionType ->
        {
            if (predicate.test(dimensionType.value()))
            {
                entries.add(dimensionType);
                try
                {   BIND_TAGS.invoke(dimensionType, ListBuilder.begin(dimensionType.tags().toList()).add(tag).build());
                } catch (Exception ignored) {}
            }
        });
        try
        {   BIND.invoke(holderSet, new ArrayList<>(entries));
        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public static void onServerStart(ServerConfigsLoadedEvent event)
    {
        InitDynamicTagsEvent tagsEvent = new InitDynamicTagsEvent(event.getServer().registryAccess());
        NeoForge.EVENT_BUS.post(tagsEvent);
    }
}
