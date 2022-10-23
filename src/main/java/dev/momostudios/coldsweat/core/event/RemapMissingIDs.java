package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.core.init.BlockInit;
import dev.momostudios.coldsweat.core.init.ItemInit;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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
            if (mapping.key.toString().equals("cold_sweat:hellspring_lamp"))
            {
                mapping.remap(ItemInit.SOULSPRING_LAMP.get());
            }
        });
    }

    @SubscribeEvent
    public static void remapMissingBlocks(RegistryEvent.MissingMappings<Block> event)
    {
        event.getAllMappings().forEach(mapping -> {
            if (mapping.key.toString().equals("cold_sweat:hearth"))
            {
                mapping.remap(BlockInit.HEARTH_BOTTOM.get());
            }
        });
    }
}
