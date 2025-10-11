package com.momosoftworks.coldsweat.api.event.vanilla;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Event used to "probe" whether the event bus is started or shutdown.<br>
 * Posting this event always returns true if the bus is started, and always returns false if the bus is shutdown.<br>
 * <br>
 * <b><u>DO NOT</u></b> un-cancel this event!<br>
 * Do not {@code @SubscribeEvent} to this event, as it is not guaranteed that the subscriber will be called.<br>
 * <br>
 * Usage:<br>
 * {@code boolean isBusStarted = MinecraftForge.EVENT_BUS.post(new ProbeEventBusEvent());}
 */
@Cancelable
public class ProbeEventBusEvent extends Event
{
    public ProbeEventBusEvent()
    {   this.setCanceled(true);
    }
}
