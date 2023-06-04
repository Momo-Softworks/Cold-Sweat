package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.client.gui.tooltip.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterTooltips
{
    @SubscribeEvent
    public static void registerTooltips(final RegisterClientTooltipComponentFactoriesEvent event)
    {
        event.register(SoulspringTooltip.class, tooltip -> new ClientSoulspringTooltip(tooltip.getFuel()));
        event.register(InsulationTooltip.class, tooltip -> new ClientInsulationTooltip(tooltip.getInsulation(), tooltip.getStack(), tooltip.getSize()));
        event.register(InsulatorTooltip.class, tooltip -> new ClientInsulatorTooltip(tooltip.getInsulationValues(), tooltip.isAdaptive()));
    }
}
