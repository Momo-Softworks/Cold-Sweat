package dev.momostudios.coldsweat.core.init;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.client.gui.BoilerScreen;
import dev.momostudios.coldsweat.client.gui.HearthScreen;
import dev.momostudios.coldsweat.client.gui.IceboxScreen;
import dev.momostudios.coldsweat.client.gui.SewingScreen;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ContainerFactoryInit
{
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event)
    {
        ScreenManager.registerFactory(ContainerInit.BOILER_CONTAINER_TYPE.get(), BoilerScreen::new);
        ScreenManager.registerFactory(ContainerInit.ICEBOX_CONTAINER_TYPE.get(), IceboxScreen::new);
        ScreenManager.registerFactory(ContainerInit.SEWING_CONTAINER_TYPE.get(), SewingScreen::new);
        ScreenManager.registerFactory(ContainerInit.HEARTH_CONTAINER_TYPE.get(), HearthScreen::new);
    }
}
