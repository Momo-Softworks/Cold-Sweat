package com.momosoftworks.coldsweat.core.init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.momosoftworks.coldsweat.client.gui.BoilerScreen;
import com.momosoftworks.coldsweat.client.gui.HearthScreen;
import com.momosoftworks.coldsweat.client.gui.IceboxScreen;
import com.momosoftworks.coldsweat.client.gui.SewingScreen;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ScreenInit
{
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event)
    {
        MenuScreens.register(MenuInit.BOILER_CONTAINER_TYPE.get(), BoilerScreen::new);
        MenuScreens.register(MenuInit.ICEBOX_CONTAINER_TYPE.get(), IceboxScreen::new);
        MenuScreens.register(MenuInit.SEWING_CONTAINER_TYPE.get(), SewingScreen::new);
        MenuScreens.register(MenuInit.HEARTH_CONTAINER_TYPE.get(), HearthScreen::new);
    }
}
