package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RemapMissingIDs
{
    @SubscribeEvent
    public static void remapMissingItems(RegistryEvent.MissingMappings<Item> event)
    {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings())
        {
            ResourceLocation key = mapping.key;
            String namespace = key.getNamespace();
            String path = key.getPath();
            if (namespace.equals(ColdSweat.MOD_ID))
            {
                switch (path)
                {
                    case "goat_fur_cap" :   mapping.remap(ModItems.GOAT_FUR_HELMET); break;
                    case "goat_fur_parka" : mapping.remap(ModItems.GOAT_FUR_CHESTPLATE); break;
                    case "goat_fur_pants" : mapping.remap(ModItems.GOAT_FUR_LEGGINGS); break;

                    case "hoglin_headpiece" : mapping.remap(ModItems.HOGLIN_HELMET); break;
                    case "hoglin_tunic" :     mapping.remap(ModItems.HOGLIN_CHESTPLATE); break;
                    case "hoglin_trousers" :  mapping.remap(ModItems.HOGLIN_LEGGINGS); break;
                    case "hoglin_hooves" :    mapping.remap(ModItems.HOGLIN_BOOTS); break;

                    case "chameleon_scale_helmet" :     mapping.remap(ModItems.CHAMELEON_HELMET); break;
                    case "chameleon_scale_chestplate" : mapping.remap(ModItems.CHAMELEON_CHESTPLATE); break;
                    case "chameleon_scale_leggings" :   mapping.remap(ModItems.CHAMELEON_LEGGINGS); break;
                    case "chameleon_scale_boots" :      mapping.remap(ModItems.CHAMELEON_BOOTS); break;
                    default: break;
                }
            }
        }
    }
}
