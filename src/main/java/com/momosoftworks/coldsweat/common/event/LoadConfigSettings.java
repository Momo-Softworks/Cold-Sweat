package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class LoadConfigSettings
{
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event)
    {   ConfigSettings.load();
    }
}
