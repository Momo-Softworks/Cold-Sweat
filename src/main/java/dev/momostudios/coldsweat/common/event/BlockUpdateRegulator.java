package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.event.common.BlockChangedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber
public class BlockUpdateRegulator
{
    public static final List<BlockChangedEvent> EVENTS = Collections.synchronizedList(new ArrayList<>());

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            synchronized (EVENTS)
            {
                EVENTS.forEach(MinecraftForge.EVENT_BUS::post);
                EVENTS.clear();
            }
        }
    }
}
