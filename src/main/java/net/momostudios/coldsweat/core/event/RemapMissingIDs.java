package net.momostudios.coldsweat.core.event;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.util.registrylists.ModItems;

@Mod.EventBusSubscriber
public class RemapMissingIDs
{
    @SubscribeEvent
    public static void remapMissingItems(RegistryEvent.MissingMappings<Item> event)
    {
        event.getAllMappings().forEach(mapping -> {
            if (mapping.key.toString().equals("cold_sweat:soulfire_lamp"))
            {
                mapping.remap(ModItems.HELLSPRING_LAMP);
            }
        });
    }
}
