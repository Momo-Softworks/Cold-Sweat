package dev.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
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
    public static boolean CUSTOM_HOTBAR = ClientSettingsConfig.getInstance().customHotbar();

    @SubscribeEvent
    public static void onRenderHotbar(RenderGameOverlayEvent.PreLayer event)
    {
        if (event.getOverlay() == ForgeIngameGui.ITEM_NAME_ELEMENT
        && Minecraft.getInstance().gui instanceof ForgeIngameGui gui && CUSTOM_HOTBAR)
        {
            event.setCanceled(true);
            PoseStack ps = event.getMatrixStack();
            ps.pushPose();
            ps.translate(0, -4, 0);
            event.getOverlay().render(gui, ps, event.getPartialTicks(), event.getWindow().getWidth(), event.getWindow().getHeight());
            ps.popPose();
        }
    }

    @SubscribeEvent
    public static void updateCustomHotbar(TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.tickCount % 20 == 0)
        {
            CUSTOM_HOTBAR = ClientSettingsConfig.getInstance().customHotbar();
        }
    }
}
