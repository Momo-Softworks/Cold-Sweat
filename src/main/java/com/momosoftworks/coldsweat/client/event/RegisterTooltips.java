package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.client.gui.tooltip.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterTooltips
{
    @SubscribeEvent
    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event)
    {
        event.register(SoulspringTooltip.class, tooltip -> new ClientSoulspringTooltip(tooltip.getFuel()));
        event.register(InsulationTooltip.class, tooltip -> new ClientInsulationTooltip(tooltip.getInsulation(), tooltip.getSlot(), tooltip.getItem()));
        event.register(InsulationAttributeTooltip.class, tooltip -> new ClientInsulationAttributeTooltip(tooltip.getOriginal(), tooltip.getFont()));
    }
}
