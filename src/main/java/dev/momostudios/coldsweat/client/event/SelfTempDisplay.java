package dev.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.PlayerHelper;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SelfTempDisplay
{
    public static PlayerTempCapability playerCap = null;
    static int iconBob = 0;

    @SubscribeEvent
    public static void eventHandler(RenderGameOverlayEvent.PostLayer event)
    {
        ClientSettingsConfig CCS = ClientSettingsConfig.getInstance();
        Minecraft mc = Minecraft.getInstance();

        if (mc.cameraEntity instanceof Player entity && event.getOverlay() == ForgeIngameGui.HOTBAR_ELEMENT && !entity.isCreative() && !entity.isSpectator())
        {
            int scaleX = event.getWindow().getGuiScaledWidth();
            int scaleY = event.getWindow().getGuiScaledHeight();

            if (playerCap == null || entity.tickCount % 40 == 0)
                playerCap = entity.getCapability(PlayerTempCapability.TEMPERATURE).orElse(new PlayerTempCapability());

            int temp = (int) playerCap.get(PlayerHelper.Types.COMPOSITE);

            int threatLevel = 0;

            ResourceLocation icon;
            int color =
                    temp > 0 ? 16744509 :
                    temp < 0 ? 4233468 :
                    11513775;
            int colorBG =
                    temp < 0 ? 1122643 :
                    temp > 0 ? 5376516 :
                    0;
            int colorBG2 =
                    CSMath.isBetween(temp, -110, -100) ? 6866175 :
                    CSMath.isBetween(temp, -120, -110) ? 7390719 :
                    CSMath.isBetween(temp, -130, -120) ? 9824511 :
                    CSMath.isBetween(temp, -140, -130) ? 12779519 :
                    temp < - 140 ?                16777215 :
                    CSMath.isBetween(temp, 100, 110) ? 16744509 :
                    CSMath.isBetween(temp, 110, 120) ? 16755544 :
                    CSMath.isBetween(temp, 120, 130) ? 16766325 :
                    CSMath.isBetween(temp, 130, 140) ? 16771509 :
                    temp > 140 ? 16777215 : 0;

            if      (temp >= 100) { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_2.png");  threatLevel = 2; }
            else if (temp >= 66)  { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_1.png");  threatLevel = 1; }
            else if (temp >= 33)  { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_0.png"); }
            else if (temp >= 0)   { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png"); }
            else if (temp >= -33) { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png"); }
            else if (temp >= -66) { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_0.png"); }
            else if (temp >= -100){ icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_1.png"); threatLevel = 1; }
            else                  { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_2.png"); threatLevel = 2; }
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            int threatOffset = 0;
            if (CCS.iconBobbing())
            {
                if (threatLevel == 1) threatOffset = iconBob;
                if (threatLevel == 2) threatOffset = entity.tickCount % 2 == 0 ? 1 : 0;
            }

            // Render Icon
            mc.getTextureManager().bindForSetup(icon);
            GuiComponent.blit(event.getMatrixStack(), (scaleX / 2) - 5 + CCS.steveHeadX(), scaleY - 51 - threatOffset + CCS.steveHeadY(), 0, 0, 10, 10, 10, 10);


            // Render Readout
            Font fontRenderer = mc.font;
            int scaledWidth = mc.getWindow().getGuiScaledWidth();
            int scaledHeight = mc.getWindow().getGuiScaledHeight();
            PoseStack matrixStack = event.getMatrixStack();

            String s = "" + (int) Math.ceil(Math.min(Math.abs(temp), 100));
            float i1 = (scaledWidth - fontRenderer.width(s)) / 2f + CCS.tempGaugeX();
            float j1 = scaledHeight - 31f - 8f + CCS.tempGaugeY();
            if (!CSMath.isBetween(temp, -100, 100))
            {
                fontRenderer.draw(matrixStack, s, i1 + 2f, j1, colorBG2);
                fontRenderer.draw(matrixStack, s, i1 - 2f, j1, colorBG2);
                fontRenderer.draw(matrixStack, s, i1, j1 + 2f, colorBG2);
                fontRenderer.draw(matrixStack, s, i1, j1 - 2f, colorBG2);
                fontRenderer.draw(matrixStack, s, i1 + 1f, j1 + 1f, colorBG2);
                fontRenderer.draw(matrixStack, s, i1 + 1f, j1 - 1f, colorBG2);
                fontRenderer.draw(matrixStack, s, i1 - 1f, j1 - 1f, colorBG2);
                fontRenderer.draw(matrixStack, s, i1 - 1f, j1 + 1f, colorBG2);
            }
            fontRenderer.draw(matrixStack, s, i1 + 1, j1, colorBG);
            fontRenderer.draw(matrixStack, s, i1 - 1, j1, colorBG);
            fontRenderer.draw(matrixStack, s, i1, j1 + 1, colorBG);
            fontRenderer.draw(matrixStack, s, i1, j1 - 1, colorBG);
            fontRenderer.draw(matrixStack, s, i1, j1, color);
        }
    }

    @SubscribeEvent
    public static void setRandomIconOffset(TickEvent.ClientTickEvent event)
    {
        iconBob = Math.random() < 0.1 ? 1 : 0;
    }
}

