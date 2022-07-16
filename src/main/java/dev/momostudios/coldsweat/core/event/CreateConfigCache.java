package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CreateConfigCache
{
    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event)
    {
        ConfigCache.getInstance().readValues(ColdSweatConfig.getInstance());
    }
}
