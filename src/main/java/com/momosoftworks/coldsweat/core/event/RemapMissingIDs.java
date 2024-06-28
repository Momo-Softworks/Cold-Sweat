package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.core.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class RemapMissingIDs
{
    //@SubscribeEvent
    //public static void remapMissingItems(MissingMappingsEvent event)
    //{
    //    if (event.getRegistry().getRegistryName().equals(ForgeRegistries.ITEMS.getRegistryName()))
    //    {
    //        for (MissingMappingsEvent.Mapping<Item> mapping : event.getAllMappings(ForgeRegistries.Keys.ITEMS))
    //        {
    //            if (mapping.getKey().equals(new ResourceLocation(ColdSweat.MOD_ID, "hellspring_lamp")))
    //            {   mapping.remap(ModItems.SOULSPRING_LAMP);
    //            }
    //            else if (mapping.getKey().getNamespace().equals(ColdSweat.MOD_ID) && mapping.getKey().toString().contains("goat_fur"))
    //            {   mapping.remap(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ColdSweat.MOD_ID, mapping.getKey().getPath().replace("goat_fur", "fur"))));
    //            }
    //        }
    //    }
    //}
}
