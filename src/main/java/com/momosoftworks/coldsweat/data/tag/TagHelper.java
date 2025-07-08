package com.momosoftworks.coldsweat.data.tag;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

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
        for (TagKey<?> tagKey : EVENT_TAGS.keySet())
        {
            if (tagKey.registry().location().equals(registry) && tagKey.location().equals(tag))
            {   return EVENT_TAGS.get(tagKey);
            }
        }
        return Collections.emptyList();
    }

    @SubscribeEvent
    public static void onServerStopped(EntityLeaveLevelEvent event)
    {
        if (event.getEntity() instanceof Player)
        {   EVENT_TAGS.clear();
        }
    }
}
