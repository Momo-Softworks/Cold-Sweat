package com.momosoftworks.coldsweat.common.event;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.configuration.Insulator;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber
public class LoadConfigSettings
{
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event)
    {   ConfigSettings.load();

        // Load JSON data-driven insulators
        RegistryAccess registries = event.getServer().registryAccess();
        registries.registryOrThrow(ModRegistries.INSULATOR)
                .holders()
                .forEach(holder ->
                {
                    Insulator settings = holder.get();
                    Insulator.Insulation insulation = settings.insulation();
                    AtomicBoolean isTag = new AtomicBoolean(false);
                    settings.itemTag().ifPresent(tag ->
                    {
                        isTag.set(true);
                        ForgeRegistries.ITEMS.tags().getTag(tag).stream().forEach(item ->
                        {
                            switch (settings.type())
                            {
                                case ITEM -> ConfigSettings.INSULATION_ITEMS.get().put(item, Pair.of(insulation.cold().orElse(0d), insulation.hot().orElse(0d)));
                                case ADAPTIVE -> ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().put(item, Pair.of(insulation.value().orElse(0d), insulation.adaptSpeed().orElse(0d)));
                                case ARMOR -> ConfigSettings.INSULATING_ARMORS.get().put(item, Pair.of(insulation.cold().orElse(0d), insulation.hot().orElse(0d)));
                                case CURIO ->
                                {
                                    if (CompatManager.isCuriosLoaded())
                                    {   ConfigSettings.INSULATING_CURIOS.get().put(item, Pair.of(insulation.cold().get(), insulation.hot().get()));
                                    }
                                    else ColdSweat.LOGGER.error("Tried to register curio insulation \"" + item + "\" but Curios is not loaded!");
                                }
                            }
                        });
                    });
                    // If the modifier defines a tag, don't look for an item
                    if (isTag.get()) return;

                    settings.itemId().ifPresent(itemId ->
                    {
                        Item item = ForgeRegistries.ITEMS.getValue(itemId);
                        if (item == null || item == Items.AIR)
                        {   ColdSweat.LOGGER.error("Tried to register insulator \"" + itemId + "\" but the item does not exist!");
                            return;
                        }
                        switch (settings.type())
                        {
                            case ITEM -> ConfigSettings.INSULATION_ITEMS.get().put(item, Pair.of(insulation.cold().orElse(0d), insulation.hot().orElse(0d)));
                            case ADAPTIVE -> ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().put(item, Pair.of(insulation.value().orElse(0d), insulation.adaptSpeed().orElse(0d)));
                            case ARMOR -> ConfigSettings.INSULATING_ARMORS.get().put(item, Pair.of(insulation.cold().orElse(0d), insulation.hot().orElse(0d)));
                            case CURIO ->
                            {
                                if (CompatManager.isCuriosLoaded())
                                {   ConfigSettings.INSULATING_CURIOS.get().put(item, Pair.of(insulation.cold().orElse(0d), insulation.hot().orElse(0d)));
                                }
                                else ColdSweat.LOGGER.error("Tried to register curio insulation \"" + item + "\" but Curios is not loaded!");
                            }
                        }
                    });
                });
    }
}
