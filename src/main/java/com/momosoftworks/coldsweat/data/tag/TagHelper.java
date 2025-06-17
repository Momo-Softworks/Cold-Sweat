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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Mod.EventBusSubscriber
public class TagHelper
{
    private static final Field CONTENTS = ObfuscationReflectionHelper.findField(HolderSet.Named.class, "f_205830_");
    static { CONTENTS.setAccessible(true); }

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
                dimensionType.bindTags(ListBuilder.begin(dimensionType.tags().toList()).add(tag).build());
            }
        });
        holderSet.bind(new ArrayList<>(entries));
    }

    @SubscribeEvent
    public static void onServerStart(ServerConfigsLoadedEvent event)
    {
        InitDynamicTagsEvent tagsEvent = new InitDynamicTagsEvent(event.getServer().registryAccess());
        MinecraftForge.EVENT_BUS.post(tagsEvent);
    }
}
