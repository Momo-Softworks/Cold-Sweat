package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.api.event.core.registry.EdiblesRegisterEvent;
import com.momosoftworks.coldsweat.common.entity.data.edible.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@EventBusSubscriber
public class EdibleInit
{
    @SubscribeEvent
    public static void onEdiblesRegister(EdiblesRegisterEvent event)
    {
        event.registerEdible(new HotBiomeEdible());
        event.registerEdible(new ColdBiomeEdible());
        event.registerEdible(new HumidBiomeEdible());
        event.registerEdible(new HealingEdible());
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event)
    {
        EdiblesRegisterEvent edibleEvent = new EdiblesRegisterEvent();
        NeoForge.EVENT_BUS.post(edibleEvent);
    }
}
