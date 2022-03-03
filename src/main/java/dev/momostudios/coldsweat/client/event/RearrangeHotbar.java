package dev.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RearrangeHotbar
{
    public static boolean customHotbar = ClientSettingsConfig.getInstance().customHotbar();
    static boolean hasShiftedUp = false;

    @SubscribeEvent
    public static void onOverlayRenderPre(RenderGameOverlayEvent.PreLayer event)
    {
        if (ClientSettingsConfig.getInstance().customHotbar())
        {
            if (hasShiftedUp)
            {
                event.getMatrixStack().translate(0, 2, 0);
                hasShiftedUp = false;
            }

            if (event.getOverlay() == ForgeIngameGui.PLAYER_HEALTH_ELEMENT ||
                event.getOverlay() == ForgeIngameGui.FOOD_LEVEL_ELEMENT ||
                event.getOverlay() == ForgeIngameGui.ARMOR_LEVEL_ELEMENT ||
                event.getOverlay() == ForgeIngameGui.MOUNT_HEALTH_ELEMENT ||
                event.getOverlay() == ForgeIngameGui.AIR_LEVEL_ELEMENT ||
                event.getOverlay() == ForgeIngameGui.ITEM_NAME_ELEMENT)
            {
                event.getMatrixStack().translate(0, -2, 0);
                hasShiftedUp = true;
            }
        }
        else
        {
            if (hasShiftedUp)
            {
                event.getMatrixStack().translate(0, 2, 0);
            }
            hasShiftedUp = false;
        }
    }

    @SubscribeEvent
    public static void updateCustomHotbar(TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.tickCount % 20 == 0)
        {
            customHotbar = ClientSettingsConfig.getInstance().customHotbar();
        }
    }
}
