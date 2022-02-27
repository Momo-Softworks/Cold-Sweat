package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.util.registrylists.ModItems;
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
        event.getAllMappings().forEach(mapping -> {
            if (mapping.key.toString().equals("cold_sweat:soulfire_lamp"))
            {
                mapping.remap(ModItems.HELLSPRING_LAMP);
            }
        });
    }
}
