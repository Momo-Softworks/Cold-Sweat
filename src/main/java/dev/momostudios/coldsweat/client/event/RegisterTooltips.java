package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.client.gui.tooltip.ClientSoulspringTooltip;
import dev.momostudios.coldsweat.client.gui.tooltip.SoulspringTooltip;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterTooltips
{
    @SubscribeEvent
    public static void registerTooltips(final FMLClientSetupEvent event)
    {
        MinecraftForgeClient.registerTooltipComponentFactory(SoulspringTooltip.class, tooltip -> new ClientSoulspringTooltip(tooltip.getFuel()));
    }
}
