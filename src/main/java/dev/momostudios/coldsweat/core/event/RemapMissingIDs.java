package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.util.registries.ModItems;
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
        if (event.getRegistry() == ForgeRegistries.ITEMS) {
            for (MissingMappingsEvent.Mapping<Item> mapping : event.getAllMappings(ForgeRegistries.Keys.ITEMS)) {
                if (mapping.getKey().toString().equals("cold_sweat:hellspring_lamp")) {
                    mapping.remap(ModItems.SOULSPRING_LAMP);
                    break;
                }
            }
        }
    }
}
