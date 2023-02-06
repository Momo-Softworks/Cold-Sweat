package dev.momostudios.coldsweat.common.event;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BlockUpdateLimiter
{
    public static int UPDATES_THIS_TICK = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            UPDATES_THIS_TICK = 0;
        }
    }
}
