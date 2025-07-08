package com.momosoftworks.coldsweat.data.tag;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
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
    public static void onServerStopped(EntityLeaveWorldEvent event)
    {
        if (event.getEntity() instanceof Player)
        {   EVENT_TAGS.clear();
        }
    }
}
