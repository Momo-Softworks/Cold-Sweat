package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.util.registries.ModItems;
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
            if (mapping.key.toString().equals("cold_sweat:hellspring_lamp"))
            {
                mapping.remap(ModItems.SOULSPRING_LAMP);
                break;
            }
        }
    }
}
