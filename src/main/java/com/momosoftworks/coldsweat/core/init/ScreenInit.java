package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.client.gui.BoilerScreen;
import com.momosoftworks.coldsweat.client.gui.HearthScreen;
import com.momosoftworks.coldsweat.client.gui.IceboxScreen;
import com.momosoftworks.coldsweat.client.gui.SewingScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ScreenInit
{
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event)
    {
        ScreenManager.register(ContainerInit.BOILER_CONTAINER_TYPE.get(), BoilerScreen::new);
        ScreenManager.register(ContainerInit.ICEBOX_CONTAINER_TYPE.get(), IceboxScreen::new);
        ScreenManager.register(ContainerInit.SEWING_CONTAINER_TYPE.get(), SewingScreen::new);
        ScreenManager.register(ContainerInit.HEARTH_CONTAINER_TYPE.get(), HearthScreen::new);
    }
}
