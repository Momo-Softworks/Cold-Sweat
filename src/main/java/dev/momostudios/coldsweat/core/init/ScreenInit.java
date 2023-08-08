package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.client.gui.BoilerScreen;
import dev.momostudios.coldsweat.client.gui.HearthScreen;
import dev.momostudios.coldsweat.client.gui.IceboxScreen;
import dev.momostudios.coldsweat.client.gui.SewingScreen;
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
