package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.config.*;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CreateConfigSettings
{
    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event)
    {
        ConfigSettings.getInstance().readValues(ColdSweatConfig.getInstance());
    }
}
