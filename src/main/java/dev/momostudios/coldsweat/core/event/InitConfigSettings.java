package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class InitConfigSettings
{
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event)
    {
        ConfigSettings.SYNCED_SETTINGS.forEach((key, value) -> value.load());
    }
}
