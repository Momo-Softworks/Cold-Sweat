package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.core.util.PlayerTemp;

@Mod.EventBusSubscriber
public class ScreenEffectOverlay
{
    /*@SubscribeEvent
    public static void displayOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
        {
            ResourceLocation coldOverlayTexture = new ResourceLocation("cold_sweat:textures/gui/overlay/overlay_freezing.png");
            ResourceLocation coldHazeTexture = new ResourceLocation("cold_sweat:textures/gui/overlay/haze_freezing.png");

            if (Minecraft.getInstance().player != null && Minecraft.getInstance().ingameGUI != null)
            {
                int scaleX = event.getWindow().getScaledWidth();
                int scaleY = event.getWindow().getScaledHeight();
                int heightFactor = (int) (scaleX / 4.81);
                int playerTemp = (int) PlayerTemp.getTemperature(Minecraft.getInstance().player, PlayerTemp.Types.BODY).get();
                int dropdownOffset = Math.abs(heightFactor * (playerTemp / 150));

                if (playerTemp < 0)
                {
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    Minecraft.getInstance().getTextureManager().bindTexture(coldOverlayTexture);
                    RenderSystem.color4f(1f, 1f, 1f, playerTemp / -150f);
                    Minecraft.getInstance().ingameGUI.blit(event.getMatrixStack(), 0, 0, 0, 0, scaleX, heightFactor, scaleX, heightFactor);
                }
            }
        }
    }*/
}
