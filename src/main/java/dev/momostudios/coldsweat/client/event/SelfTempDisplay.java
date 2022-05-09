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
    public static ITemperatureCap PLAYER_CAP = null;
    static int PLAYER_TEMP = 0;
    static int ICON_BOB = 0;
    static int ICON_BOB_TIMER = 0;

    static int CURRENT_ICON = 0;
    static int PREV_ICON = 0;
    static int TRANSITION_PROGRESS = 0;
    static int BLEND_TIME = 10;

    static int TEMP_SEVERITY = 0;

    @SubscribeEvent
    public static void handleTransition(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().cameraEntity instanceof Player entity)
        {
            if (PLAYER_CAP == null || entity.tickCount % 40 == 0)
                PLAYER_CAP = entity.getCapability(ModCapabilities.PLAYER_TEMPERATURE).orElse(new PlayerTempCapability());

            TEMP_SEVERITY = getTempSeverity(PLAYER_TEMP);

            PLAYER_TEMP = (int) PLAYER_CAP.get(Temperature.Types.BODY);

            int neededIcon = (int) CSMath.clamp(TEMP_SEVERITY, -3, 3);

            // Hot Temperatures
            if (CURRENT_ICON != neededIcon)
            {
                CURRENT_ICON = neededIcon;
                TRANSITION_PROGRESS = 0;
            }

            // Tick the transition progress
            if (PREV_ICON != CURRENT_ICON)
            {
                TRANSITION_PROGRESS++;

                if (TRANSITION_PROGRESS >= BLEND_TIME)
                {
                    PREV_ICON = CURRENT_ICON;
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderElements(RenderGameOverlayEvent.Post event)
    {
        ClientSettingsConfig CCS = ClientSettingsConfig.getInstance();
        Minecraft mc = Minecraft.getInstance();

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && mc.cameraEntity instanceof Player player && !player.isCreative() && !player.isSpectator() && PLAYER_CAP != null)
        {
            int scaleX = event.getWindow().getGuiScaledWidth();
            int scaleY = event.getWindow().getGuiScaledHeight();

            int threatLevel = switch (CURRENT_ICON)
            {
                case  2,-2 -> 1;
                case  3,-3 -> 2;
                default -> 0;
            };

            ResourceLocation icon = switch (CURRENT_ICON)
            {
                case  1 ->   new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_0.png");
                case  2 ->   new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_1.png");
                case  3 ->   new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_2.png");
                case -1 ->   new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_0.png");
                case -2 ->   new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_1.png");
                case -3 ->   new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_2.png");
                default ->   new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png");
            };
            ResourceLocation lastIcon = switch (PREV_ICON)
            {
                case 1  ->  new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_0.png");
                case 2  ->  new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_1.png");
                case 3  ->  new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_2.png");
                case -1 ->  new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_0.png");
                case -2 ->  new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_1.png");
                case -3 ->  new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_2.png");
                default ->  new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png");
            };

            // Get text color
            int color =
                    PLAYER_TEMP > 0 ? 16744509 :
                    PLAYER_TEMP < 0 ? 4233468 :
                    11513775;
            // Get outline color
            int colorBG =
                    PLAYER_TEMP < 0 ? 1122643 :
                    PLAYER_TEMP > 0 ? 5376516 :
                    0;
            // Get the outer border color when readout is > 100
            int colorBG2 = switch (TEMP_SEVERITY)
            {
                case  7, -7 -> 16777215;
                case  6 -> 16771509;
                case  5 -> 16766325;
                case  4 -> 16755544;
                case  3 -> 16744509;
                case -3 -> 6866175;
                case -4 -> 7390719;
                case -5 -> 9824511;
                case -6 -> 12779519;
                default -> 0;
            };

            RenderSystem.defaultBlendFunc();

            int threatOffset = 0;
            if (CCS.iconBobbing())
            {
                if (threatLevel == 1) threatOffset = ICON_BOB;
                else if (threatLevel == 2) threatOffset = player.tickCount % 2 == 0 ? 1 : 0;
            }

            event.getMatrixStack().pushPose();
            // Render Icon
            if (TRANSITION_PROGRESS < BLEND_TIME)
            {
                RenderSystem.setShaderTexture(0, lastIcon);
                GuiComponent.blit(event.getMatrixStack(), (scaleX / 2) - 5 + CCS.tempIconX(), scaleY - 53 - threatOffset + CCS.tempIconY(), 0, 0, 10, 10, 10, 10);
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1, 1, 1, (mc.getFrameTime() + TRANSITION_PROGRESS) / BLEND_TIME);
            }
            RenderSystem.setShaderTexture(0, icon);
            GuiComponent.blit(event.getMatrixStack(), (scaleX / 2) - 5 + CCS.tempIconX(), scaleY - 53 - threatOffset + CCS.tempIconY(), 0, 0, 10, 10, 10, 10);
            RenderSystem.setShaderColor(1, 1, 1, 1);


            // Render Readout
            Font fontRenderer = mc.font;
            int scaledWidth = mc.getWindow().getGuiScaledWidth();
            int scaledHeight = mc.getWindow().getGuiScaledHeight();
            PoseStack matrixStack = event.getMatrixStack();

            String s = "" + (int) Math.ceil(Math.min(Math.abs(PLAYER_TEMP), 100));
            float x = (scaledWidth - fontRenderer.width(s)) / 2f + CCS.tempReadoutX();
            float y = scaledHeight - 31f - 10f + CCS.tempReadoutY();

            if (!CSMath.isBetween(PLAYER_TEMP, -99, 99))
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
            event.getMatrixStack().popPose();
        }
    }

    @SubscribeEvent
    public static void setRandomIconOffset(TickEvent.ClientTickEvent event)
    {
        ICON_BOB_TIMER++;
        ICON_BOB = Math.random() < 0.3 && ICON_BOB_TIMER >= 3 ? 1 : 0;
        if (ICON_BOB_TIMER >= 3) ICON_BOB_TIMER = 0;
    }

    static int getTempSeverity(int temp)
    {
        return temp >= 140  ?  7
             : temp >= 130  ?  6
             : temp >= 120  ?  5
             : temp >= 110  ?  4
             : temp >= 100  ?  3
             : temp >= 66   ?  2
             : temp >= 33   ?  1
             : temp >= 0    ?  0
             : temp >= -32  ?  0
             : temp >= -65  ? -1
             : temp >= -99  ? -2
             : temp >= -109 ? -3
             : temp >= -119 ? -4
             : temp >= -129 ? -5
             : temp >= -139 ? -6
             : -7;
    }
}

