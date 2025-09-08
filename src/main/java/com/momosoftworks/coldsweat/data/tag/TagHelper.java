package com.momosoftworks.coldsweat.data.tag;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class TagHelper
{
    public static RegistryAccess REGISTRY_ACCESS = null;
    public static Map<TagKey<?>, Collection<Holder<?>>> EVENT_TAGS = new HashMap<>();

    public static Collection<Holder<?>> getTagValues(ResourceLocation registry, ResourceLocation tag)
    {
        for (Map.Entry<TagKey<?>, Collection<Holder<?>>> entry : EVENT_TAGS.entrySet())
        {
            TagKey<?> tagKey = entry.getKey();
            if (tagKey.registry().location().equals(registry) && tagKey.location().equals(tag))
            {   return entry.getValue();
            }
        }
        return Collections.emptyList();
    }

    @SubscribeEvent
    public static void onServerStopped(TagsUpdatedEvent event)
    {   EVENT_TAGS.clear();
    }
}
