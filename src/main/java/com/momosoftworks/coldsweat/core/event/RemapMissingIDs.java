package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

@Mod.EventBusSubscriber
public class RemapMissingIDs
{
    @SubscribeEvent
    public static void remapMissingItems(MissingMappingsEvent event)
    {
        if (event.getRegistry().getRegistryName().equals(ForgeRegistries.ITEMS.getRegistryName()))
        {
            for (MissingMappingsEvent.Mapping<Item> mapping : event.getAllMappings(ForgeRegistries.Keys.ITEMS))
            {
                ResourceLocation key = mapping.getKey();
                String namespace = key.getNamespace();
                String path = key.getPath();
                // Remap fur to goat_fur
                if (namespace.equals(ColdSweat.MOD_ID) && path.contains("fur"))
                {   mapping.remap(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ColdSweat.MOD_ID, path.replace("fur", "goat_fur"))));
                }
            }
        }
    }
}
