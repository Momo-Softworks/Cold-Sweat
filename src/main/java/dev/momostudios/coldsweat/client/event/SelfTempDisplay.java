package dev.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.PlayerTempCapability;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.util.math.CSMath;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SelfTempDisplay
{
    public static ITemperatureCap playerCap = null;
    static int playerTemp = 0;
    static int iconBob = 0;
    static int bobTimer = 0;

    static int currentIcon = 0;
    static int prevIcon = 0;
    static int transitionProgress = 0;

    static int blendTime = 5;

    @SubscribeEvent
    public static void handleTransition(TickEvent.ClientTickEvent event)
    {
        blendTime = 10;
        if (Minecraft.getInstance().cameraEntity instanceof Player entity)
        {
            if (playerCap == null || entity.tickCount % 40 == 0)
                playerCap = entity.getCapability(ModCapabilities.PLAYER_TEMPERATURE).orElse(new PlayerTempCapability());

            playerTemp = (int) playerCap.get(Temperature.Types.BODY);

            int neededIcon = CSMath.isBetween(playerTemp, 33, 65)   ?  1 :
                             CSMath.isBetween(playerTemp, 66, 99)   ?  2 :
                             playerTemp >= 100                      ?  3 :
                             CSMath.isBetween(playerTemp, -65, -33) ? -1 :
                             CSMath.isBetween(playerTemp, -99, -66) ? -2 :
                             playerTemp <= -100                     ? -3 : 0;

            // Hot Temperatures
            if (currentIcon != neededIcon)
            {
                currentIcon = neededIcon;
                transitionProgress = 0;
            }

            // Tick the transition progress
            if (prevIcon != currentIcon)
            {
                transitionProgress++;

                if (transitionProgress >= blendTime)
                {
                    prevIcon = currentIcon;
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderElements(RenderGameOverlayEvent.Post event)
    {
        ClientSettingsConfig CCS = ClientSettingsConfig.getInstance();
        Minecraft mc = Minecraft.getInstance();

        if (mc.cameraEntity instanceof Player entity && !entity.isCreative() && !entity.isSpectator() && playerCap != null)
        {
            int scaleX = event.getWindow().getGuiScaledWidth();
            int scaleY = event.getWindow().getGuiScaledHeight();

            int threatLevel = 0;

            ResourceLocation icon;
            ResourceLocation lastIcon;
            int color =
                    playerTemp > 0 ? 16744509 :
                    playerTemp < 0 ? 4233468 :
                    11513775;
            int colorBG =
                    playerTemp < 0 ? 1122643 :
                    playerTemp > 0 ? 5376516 :
                    0;
            int colorBG2 =
                    CSMath.isBetween(playerTemp, -110, -100) ? 6866175 :
                    CSMath.isBetween(playerTemp, -120, -110) ? 7390719 :
                    CSMath.isBetween(playerTemp, -130, -120) ? 9824511 :
                    CSMath.isBetween(playerTemp, -140, -130) ? 12779519 :
                    playerTemp < - 140 ?                16777215 :
                    CSMath.isBetween(playerTemp, 100, 110) ? 16744509 :
                    CSMath.isBetween(playerTemp, 110, 120) ? 16755544 :
                    CSMath.isBetween(playerTemp, 120, 130) ? 16766325 :
                    CSMath.isBetween(playerTemp, 130, 140) ? 16771509 :
                    playerTemp > 140 ? 16777215 : 0;

            switch (currentIcon)
            {
                case  1 ->   icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_0.png");
                case  2 -> { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_1.png");  threatLevel = 1; }
                case  3 -> { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_2.png");  threatLevel = 2; }
                case -1 ->   icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_0.png");
                case -2 -> { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_1.png"); threatLevel = 1; }
                case -3 -> { icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_2.png"); threatLevel = 2; }
                default ->  icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png");
            }

            switch (prevIcon)
            {
                case 1  ->  lastIcon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_0.png");
                case 2  ->  lastIcon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_1.png");
                case 3  ->  lastIcon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_2.png");
                case -1 ->  lastIcon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_0.png");
                case -2 ->  lastIcon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_1.png");
                case -3 ->  lastIcon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_2.png");
                default ->  lastIcon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png");
            }

            RenderSystem.defaultBlendFunc();

            int threatOffset = 0;
            if (CCS.iconBobbing())
            {
                if (threatLevel == 1) threatOffset = iconBob;
                if (threatLevel == 2) threatOffset = entity.tickCount % 2 == 0 ? 1 : 0;
            }

            // Render Icon
            if (transitionProgress < blendTime)
            {
                RenderSystem.setShaderTexture(0, lastIcon);
                GuiComponent.blit(event.getMatrixStack(), (scaleX / 2) - 5 + CCS.tempIconX(), scaleY - 53 - threatOffset + CCS.tempIconY(), 0, 0, 10, 10, 10, 10);
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1, 1, 1, (mc.getFrameTime() + transitionProgress) / blendTime);
            }
            RenderSystem.setShaderTexture(0, icon);
            GuiComponent.blit(event.getMatrixStack(), (scaleX / 2) - 5 + CCS.tempIconX(), scaleY - 53 - threatOffset + CCS.tempIconY(), 0, 0, 10, 10, 10, 10);
            RenderSystem.setShaderColor(1, 1, 1, 1);


            // Render Readout
            Font fontRenderer = mc.font;
            int scaledWidth = mc.getWindow().getGuiScaledWidth();
            int scaledHeight = mc.getWindow().getGuiScaledHeight();
            PoseStack matrixStack = event.getMatrixStack();

            String s = "" + (int) Math.ceil(Math.min(Math.abs(playerTemp), 100));
            float x = (scaledWidth - fontRenderer.width(s)) / 2f + CCS.tempReadoutX();
            float y = scaledHeight - 31f - 10f + CCS.tempReadoutY();
            if (!CSMath.isBetween(playerTemp, -100, 100))
            {
                fontRenderer.draw(matrixStack, s, x + 2f, y, colorBG2);
                fontRenderer.draw(matrixStack, s, x - 2f, y, colorBG2);
                fontRenderer.draw(matrixStack, s, x, y + 2f, colorBG2);
                fontRenderer.draw(matrixStack, s, x, y - 2f, colorBG2);
                fontRenderer.draw(matrixStack, s, x + 1f, y + 1f, colorBG2);
                fontRenderer.draw(matrixStack, s, x + 1f, y - 1f, colorBG2);
                fontRenderer.draw(matrixStack, s, x - 1f, y - 1f, colorBG2);
                fontRenderer.draw(matrixStack, s, x - 1f, y + 1f, colorBG2);
            }
            fontRenderer.draw(matrixStack, s, x + 1, y, colorBG);
            fontRenderer.draw(matrixStack, s, x - 1, y, colorBG);
            fontRenderer.draw(matrixStack, s, x, y + 1, colorBG);
            fontRenderer.draw(matrixStack, s, x, y - 1, colorBG);
            fontRenderer.draw(matrixStack, s, x, y, color);
        }
    }

    @SubscribeEvent
    public static void setRandomIconOffset(TickEvent.ClientTickEvent event)
    {
        bobTimer++;
        iconBob = Math.random() < 0.3 && bobTimer >= 3 ? 1 : 0;
        if (bobTimer >= 3) bobTimer = 0;
    }
}

