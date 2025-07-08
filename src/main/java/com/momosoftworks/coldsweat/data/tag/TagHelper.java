package com.momosoftworks.coldsweat.data.tag;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class TagHelper
{
    private static final Field CONTENTS = ObfuscationReflectionHelper.findField(HolderSet.Named.class, "f_205830_");
    static { CONTENTS.setAccessible(true); }

    public static ReloadableServerResources SERVER_RESOURCES = null;
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
    public static void onResourceReload(AddReloadListenerEvent event)
    {   SERVER_RESOURCES = event.getServerResources();
    }

    @SubscribeEvent
    public static void onServerStopped(EntityLeaveLevelEvent event)
    {
        if (event.getEntity() instanceof Player)
        {   SERVER_RESOURCES = null;
            EVENT_TAGS.clear();
        }
    }
}
