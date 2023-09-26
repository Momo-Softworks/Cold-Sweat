package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.event.core.EdiblesRegisterEvent;
import com.momosoftworks.coldsweat.common.entity.data.edible.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RegisterChameleonEdibles
{
    @SubscribeEvent
    public static void onWorldLoaded(ServerStartedEvent event)
    {   EdiblesRegisterEvent edibleEvent = new EdiblesRegisterEvent();
        MinecraftForge.EVENT_BUS.post(edibleEvent);
    }

    @SubscribeEvent
    public static void onEdiblesRegister(EdiblesRegisterEvent event)
    {   event.registerEdible(new HotBiomeEdible());
        event.registerEdible(new ColdBiomeEdible());
        event.registerEdible(new HumidBiomeEdible());
        event.registerEdible(new HealingEdible());
        event.registerEdible(new HealingEdible());
    }
}
