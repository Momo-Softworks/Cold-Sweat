package dev.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.core.capabilities.PlayerTempCapability;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TempModifiersClient
{
    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap -> cap.tickClient(Minecraft.getInstance().player));
        }
    }
}
