package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.MissingObjectEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class RemapMissingIDs
{
    @SubscribeEvent
    public static void remapMissingItems(MissingObjectEvent<Item> event)
    {
        ResourceLocation key = event.getKey();
        String namespace = key.getNamespace();
        String path = key.getPath();
        // Remap fur to goat_fur
        if (namespace.equals(ColdSweat.MOD_ID) && path.contains("fur"))
        {   event.remap(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, path.replace("fur", "goat_fur")));
        }
    }
}
