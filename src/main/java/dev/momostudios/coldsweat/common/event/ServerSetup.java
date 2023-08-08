package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.event.core.EdiblesRegisterEvent;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.core.init.TempModifierInit;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod.EventBusSubscriber
public class ServerSetup
{
    @SubscribeEvent
    public static void serverStartSetup(FMLServerStartingEvent event)
    {   // Load config settings
        ConfigSettings.load();
        // Register temperature modifiers
        TempModifierInit.buildRegistries();
        // Register chameleon edibles
        EdiblesRegisterEvent edibleEvent = new EdiblesRegisterEvent();
        MinecraftForge.EVENT_BUS.post(edibleEvent);
    }
}
