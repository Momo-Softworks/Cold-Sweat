package com.momosoftworks.coldsweat.api.event.vanilla;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Event used to "probe" whether the event bus is started or shutdown.<br>
 * Posting this event always returns true if the bus is started, and always returns false if the bus is shutdown.<br>
 * <br>
 * Usage:<br>
 * {@code boolean isBusStarted = MinecraftForge.EVENT_BUS.post(new ProbeEventBusEvent());}
 */
@EventBusSubscriber
public class ProbeEventBusEvent extends Event
{
    private boolean success = false;

    public boolean isBusEnabled()
    {   return success;
    }

    @SubscribeEvent
    public static void setState(ProbeEventBusEvent event)
    {   event.success = true;
    }
}
