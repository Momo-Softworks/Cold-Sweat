package dev.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RearrangeHotbar
{
    public static boolean customHotbar = ClientSettingsConfig.getInstance().customHotbar();

    @SubscribeEvent
    public static void updateCustomHotbar(TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.tickCount % 20 == 0)
        {
            customHotbar = ClientSettingsConfig.getInstance().customHotbar();
        }
    }
}
