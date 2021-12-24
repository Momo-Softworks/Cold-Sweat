package net.momostudios.coldsweat.common.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.core.event.csevents.TempModifierEvent;

@Mod.EventBusSubscriber
public class StorePreHearthTemp
{
    @SubscribeEvent
    public static void storePreHearthTemp(TempModifierEvent.Tick.Pre event)
    {
        event.player.getPersistentData().putDouble("preHearthTemp", event.getTemperature().get());
    }
}
