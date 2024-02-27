package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.util.ItemData;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.configuration.Insulator;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class LoadConfigSettings
{
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event)
    {
        ConfigSettings.load();

        // Load JSON data-driven insulators
        RegistryAccess registries = event.getServer().registryAccess();
        registries.registryOrThrow(ModRegistries.INSULATOR)
        .holders()
        .forEach(holder ->
        {
            Insulator insulator = holder.value();
            Insulation insulation = insulator.getInsulation();
            CompoundTag nbt = insulator.nbt().orElse(new CompoundTag());
            // If the item is defined, add it to the appropriate map
            insulator.item().ifPresent(itemOrList ->
            {
                // If the item is single, write the insulation value
                itemOrList.ifLeft(item -> addItemConfig(item, insulation, insulator.type(), nbt));
                // If the item is a list, write the insulation value for each item
                itemOrList.ifRight(list ->
                {
                    for (Item item : list)
                    {   addItemConfig(item, insulation, insulator.type(), nbt);
                    }
                });
            });
            // If the tag is defined, add all items in the tag to the appropriate map
            insulator.tag().ifPresent(tag ->
            {
                ForgeRegistries.ITEMS.tags().getTag(tag).stream().forEach(item ->
                {   addItemConfig(item, insulation, insulator.type(), nbt);
                });
            });
        });
    }

    private static void addItemConfig(Item item, Insulation insulation, InsulationType type, CompoundTag nbt)
    {
        switch (type)
        {
            case ITEM -> ConfigSettings.INSULATION_ITEMS.get().put(new ItemData(item, nbt), insulation);
            case ARMOR -> ConfigSettings.INSULATING_ARMORS.get().put(new ItemData(item, nbt), insulation);
            case CURIO ->
            {
                if (CompatManager.isCuriosLoaded())
                {   ConfigSettings.INSULATING_CURIOS.get().put(new ItemData(item, nbt), insulation);
                }
            }
        }
    }
}
