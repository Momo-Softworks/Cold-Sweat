package net.momostudios.coldsweat.core.init;

import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.momostudios.coldsweat.core.capabilities.ITemperatureCapability;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapStorage;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityInit
{
    // Register Caps
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        CapabilityManager.INSTANCE.register(ITemperatureCapability.class, new PlayerTempCapStorage(), PlayerTempCapability::new);
    }
}
