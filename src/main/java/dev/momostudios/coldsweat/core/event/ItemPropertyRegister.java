package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.client.itemproperties.HellspringLampOverride;
import dev.momostudios.coldsweat.client.itemproperties.ThermometerOverride;
import dev.momostudios.coldsweat.util.registrylists.ModItems;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

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
        ItemModelsProperties.registerProperty(ModItems.HELLSPRING_LAMP, new ResourceLocation("cold_sweat:soulfire_state"), new HellspringLampOverride());
        ItemModelsProperties.registerProperty(ModItems.THERMOMETER, new ResourceLocation("cold_sweat:temperature"), new ThermometerOverride());
    }
}
