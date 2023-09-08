package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.util.registries.ModItems;
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
                if (mapping.getKey().equals(new ResourceLocation(ColdSweat.MOD_ID, "hellspring_lamp")))
                {   mapping.remap(ModItems.SOULSPRING_LAMP);
                }
                else if (mapping.getKey().getNamespace().equals(ColdSweat.MOD_ID) && mapping.getKey().toString().contains("goat_fur"))
                {   mapping.remap(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ColdSweat.MOD_ID, mapping.getKey().getPath().replace("goat_fur", "fur"))));
                }
            }
        }
    }
}
