package dev.momosoftworks.coldsweat.common.event;

import dev.momosoftworks.coldsweat.common.entity.Chameleon;
import dev.momosoftworks.coldsweat.core.init.EntityInit;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterAttributes
{
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event)
    {
        event.put(EntityInit.CHAMELEON.get(), Chameleon.createAttributes().build());
    }
}
