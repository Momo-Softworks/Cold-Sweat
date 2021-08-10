package net.momostudios.coldsweat.util.client;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.gui.screen.BoilerScreen;
import net.momostudios.coldsweat.util.init.ContainerInit;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventBusSubscriber
{
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event)
    {
        ScreenManager.registerFactory(ContainerInit.BOILER_CONTAINER_TYPE.get(), BoilerScreen::new);
    }
}
