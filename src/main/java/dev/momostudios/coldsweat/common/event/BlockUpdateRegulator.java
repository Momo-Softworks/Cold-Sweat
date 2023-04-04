package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.event.common.BlockChangedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class BlockUpdateRegulator
{
    public static List<BlockChangedEvent> EVENTS = new ArrayList<>();

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            for (BlockChangedEvent changedEvent : EVENTS)
            {
                MinecraftForge.EVENT_BUS.post(changedEvent);
            }
            EVENTS.clear();
        }
    }
}
