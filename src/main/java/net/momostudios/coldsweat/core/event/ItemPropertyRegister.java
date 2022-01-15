package net.momostudios.coldsweat.core.event;

import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.momostudios.coldsweat.client.itemproperties.SoulfireLampOverride;
import net.momostudios.coldsweat.client.itemproperties.ThermometerOverride;
import net.momostudios.coldsweat.util.registrylists.ModItems;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ItemPropertyRegister
{
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(ItemPropertyRegister::registerPropertyOverride);
    }

    public static void registerPropertyOverride()
    {
        ItemModelsProperties.registerProperty(ModItems.SOULFIRE_LAMP, new ResourceLocation("cold_sweat:soulfire_state"), new SoulfireLampOverride());
        ItemModelsProperties.registerProperty(ModItems.THERMOMETER, new ResourceLocation("cold_sweat:temperature"), new ThermometerOverride());
    }
}
