package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.event.core.EdiblesRegisterEvent;
import com.momosoftworks.coldsweat.common.entity.data.edible.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RegisterChameleonEdibles
{
    @SubscribeEvent
    public static void onEdiblesRegister(EdiblesRegisterEvent event)
    {   event.registerEdible(new HotBiomeEdible());
        event.registerEdible(new ColdBiomeEdible());
        event.registerEdible(new HumidBiomeEdible());
        event.registerEdible(new HealingEdible());
        event.registerEdible(new HealingEdible());
    }
}
