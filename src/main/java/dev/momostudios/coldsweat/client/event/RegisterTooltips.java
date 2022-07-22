package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.client.gui.tooltip.ClientHellspringTooltip;
import dev.momostudios.coldsweat.client.gui.tooltip.HellspringTooltip;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RegisterTooltips
{
    @SubscribeEvent
    public static void registerTooltips(FMLClientSetupEvent event)
    {
        MinecraftForgeClient.registerTooltipComponentFactory(HellspringTooltip.class, tooltip -> new ClientHellspringTooltip(tooltip.getFuel()));
    }
}
