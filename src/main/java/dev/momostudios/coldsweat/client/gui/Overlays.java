package dev.momostudios.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.capability.PlayerTempCap;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class Overlays
{
    public static ITemperatureCap PLAYER_CAP = new PlayerTempCap();

    // Stuff for world temperature
    public static double WORLD_TEMP = 0;
    static boolean SHOW_WORLD_TEMP = false;
    static double PREV_WORLD_TEMP = 0;
    static double MAX_OFFSET = 0;
    static double MIN_OFFSET = 0;

    // Stuff for body temperature
    public static double BODY_TEMP = 0;
    static boolean SHOW_BODY_TEMP = false;
    static double PREV_BODY_TEMP = 0;
    static int BLEND_BODY_TEMP = 0;
    static int ICON_BOB = 0;
    static int BODY_ICON = 0;
    static int PREV_BODY_ICON = 0;
    static int BODY_TRANSITION_PROGRESS = 0;
    static int BODY_BLEND_TIME = 10;
    static int BODY_TEMP_SEVERITY = 0;

    static ClientSettingsConfig CLIENT_CONFIG = ClientSettingsConfig.getInstance();

    public static void registerOverlays()
    {
        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.CHAT_PANEL_ELEMENT, "World Temp", (gui, poseStack, partialTick, width, height) ->
        {
            gui.setupOverlayRenderState(true, false);

            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null && SHOW_WORLD_TEMP && !Minecraft.getInstance().options.hideGui)
            {
                double min = ConfigSettings.getInstance().minTemp + MIN_OFFSET;
                double max = ConfigSettings.getInstance().maxTemp + MAX_OFFSET;

                // Get player world temperature
                double temp = CSMath.convertUnits(WORLD_TEMP, CLIENT_CONFIG.celsius() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);

                // Get the temperature severity
                int severity = getWorldSeverity(temp, min, max);

                // Set text color
                int color = switch (severity)
                        {
                            case  2,3  -> 16297781;
                            case  4    -> 16728089;
                            case -2,-3 -> 8443135;
                            case -4    -> 4236031;
                            default -> 14737376;
                        };


                /* Render gauge */

                poseStack.pushPose();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);

                // Set gauge texture
                RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/overlay/world_temp_gauge.png"));

                // Render frame
                GuiComponent.blit(poseStack, (width / 2) + 94 + CLIENT_CONFIG.worldGaugeX(), height - 19 + CLIENT_CONFIG.worldGaugeY(), 0, 64 - severity * 16, 25, 16, 25, 144);

                RenderSystem.disableBlend();

                // Sets the text bobbing offset (or none if disabled)
                int bob = CLIENT_CONFIG.iconBobbing() && !CSMath.isInRange(temp, min, max) && player.tickCount % 2 == 0 ? 1 : 0;

                // Render text
                int blendedTemp = (int) CSMath.blend(PREV_WORLD_TEMP, WORLD_TEMP, Minecraft.getInstance().getFrameTime(), 0, 1);

                Minecraft.getInstance().font.draw(poseStack, "" + (blendedTemp + CLIENT_CONFIG.tempOffset()) + "",
                        /* X */ width / 2f + 107 + (Integer.toString(blendedTemp + CLIENT_CONFIG.tempOffset()).length() * -3) + CLIENT_CONFIG.worldGaugeX(),
                        /* Y */ height - 15 - bob + CLIENT_CONFIG.worldGaugeY(), color);
                poseStack.popPose();
            }
        });

        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.CHAT_PANEL_ELEMENT, "Body Temp", (gui, poseStack, partialTick, width, height) ->
        {
            gui.setupOverlayRenderState(true, false);
            Minecraft mc = Minecraft.getInstance();

            if (mc.cameraEntity instanceof Player player && SHOW_BODY_TEMP && !Minecraft.getInstance().options.hideGui)
            {
                // Blend body temp (per frame)
                BLEND_BODY_TEMP = (int) CSMath.blend(PREV_BODY_TEMP, BODY_TEMP, Minecraft.getInstance().getFrameTime(), 0, 1);

                // Get text color
                int color = switch (BODY_TEMP_SEVERITY)
                        {
                            case  7, -7 -> 16777215;
                            case  6 -> 16777132;
                            case  5 -> 16767856;
                            case  4 -> 16759634;
                            case  3 -> 16751174;
                            case -3 -> 6078975;
                            case -4 -> 7528447;
                            case -5 -> 8713471;
                            case -6 -> 11599871;
                            default -> BLEND_BODY_TEMP > 0 ? 16744509
                                    : BLEND_BODY_TEMP < 0 ? 4233468
                                    : 11513775;
                        };

                // Get the outer border color when readout is > 100
                int colorBG =
                        BLEND_BODY_TEMP < 0 ? 1122643 :
                                BLEND_BODY_TEMP > 0 ? 5376516 :
                                        0;

                int bobLevel = Math.min(Math.abs(BODY_TEMP_SEVERITY), 3);
                int threatOffset =
                        !CLIENT_CONFIG.iconBobbing() ? 0
                                : bobLevel == 2 ? ICON_BOB
                                : bobLevel == 3 ? player.tickCount % 2
                                : 0;

                RenderSystem.defaultBlendFunc();

                // Render old icon (if blending)
                RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/overlay/body_temp_gauge.png"));
                if (BODY_TRANSITION_PROGRESS < BODY_BLEND_TIME)
                {
                    GuiComponent.blit(poseStack, (width / 2) - 5 + CLIENT_CONFIG.bodyIconX(), height - 53 - threatOffset + CLIENT_CONFIG.bodyIconY(), 0, 30 - PREV_BODY_ICON * 10, 10, 10, 10, 70);
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1, 1, 1, (mc.getFrameTime() + BODY_TRANSITION_PROGRESS) / BODY_BLEND_TIME);
                }
                // Render new icon on top of old icon (if blending)
                // Otherwise this is just the regular icon
                GuiComponent.blit(poseStack, (width / 2) - 5 + CLIENT_CONFIG.bodyIconX(), height - 53 - threatOffset + CLIENT_CONFIG.bodyIconY(), 0, 30 - BODY_ICON * 10, 10, 10, 10, 70);
                RenderSystem.setShaderColor(1, 1, 1, 1);

                // Render Readout
                Font font = mc.font;
                int scaledWidth = mc.getWindow().getGuiScaledWidth();
                int scaledHeight = mc.getWindow().getGuiScaledHeight();

                String s = "" + Math.min(Math.abs(BLEND_BODY_TEMP), 100);
                float x = (scaledWidth - font.width(s)) / 2f + CLIENT_CONFIG.bodyReadoutX();
                float y = scaledHeight - 31f - 10f + CLIENT_CONFIG.bodyReadoutY();

                // Draw the outline
                font.draw(poseStack, s, x + 1, y, colorBG);
                font.draw(poseStack, s, x - 1, y, colorBG);
                font.draw(poseStack, s, x, y + 1, colorBG);
                font.draw(poseStack, s, x, y - 1, colorBG);

                // Draw the readout
                font.draw(poseStack, s, x, y, color);
            }
        });
    }

    // Handle temperature blending and transitions
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().cameraEntity instanceof Player player)
        {
            // Ensure player temp capability is stored
            if (PLAYER_CAP == null || player.tickCount % 40 == 0)
            {
                PLAYER_CAP = player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).orElse(new PlayerTempCap());
            }


            /* World Temp */

            if (SHOW_WORLD_TEMP)
            {
                boolean celsius = CLIENT_CONFIG.celsius();

                // Get temperature in actual degrees
                double worldTemp = PLAYER_CAP.getTemp(Temperature.Type.WORLD);
                double realTemp = CSMath.convertUnits(worldTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true);

                // Calculate the blended world temp for this tick
                double diff = realTemp - WORLD_TEMP;
                PREV_WORLD_TEMP = WORLD_TEMP;
                WORLD_TEMP += Math.abs(diff) <= 0.5 ? diff : diff / 2d;

                // Update max/min offset
                MAX_OFFSET = PLAYER_CAP.getTemp(Temperature.Type.MAX);
                MIN_OFFSET = PLAYER_CAP.getTemp(Temperature.Type.MIN);
            }


            /* Body Temp */

            // Blend body temp (per tick)
            PREV_BODY_TEMP = BODY_TEMP;
            BODY_TEMP += (PLAYER_CAP.getTemp(Temperature.Type.BODY) - BODY_TEMP) / 5;

            // Handle effects for the icon (bobbing, stage, transition)
            if (SHOW_BODY_TEMP)
            {
                // Get icon bob
                ICON_BOB = player.tickCount % 3 == 0 && Math.random() < 0.3 ? 1 : 0;

                // Get the severity of the player's body temperature
                BODY_TEMP_SEVERITY = getBodySeverity(BLEND_BODY_TEMP);

                // Get the icon to be displayed
                int neededIcon = CSMath.clamp(BODY_TEMP_SEVERITY, -3, 3);

                // Start transition
                if (BODY_ICON != neededIcon)
                {
                    BODY_ICON = neededIcon;
                    BODY_TRANSITION_PROGRESS = 0;
                }

                // Tick the transition progress
                if (PREV_BODY_ICON != BODY_ICON
                && BODY_TRANSITION_PROGRESS++ >= BODY_BLEND_TIME)
                {
                    PREV_BODY_ICON = BODY_ICON;
                }
            }
        }
    }

    @SubscribeEvent
    public static void updateDisplayCondition(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            Player player = event.player;
            SHOW_WORLD_TEMP = !ConfigSettings.getInstance().requireThermometer
                            || player.getInventory().items.stream().limit(9).anyMatch(stack -> stack.getItem() == ModItems.THERMOMETER)
                            || player.getOffhandItem().getItem() == ModItems.THERMOMETER;
            SHOW_BODY_TEMP = !player.isCreative() && !player.isSpectator();
        }
    }

    static int getWorldSeverity(double temp, double min, double max)
    {
        return (int) CSMath.blend(-4, 4, temp, min, max);
    }

    static int getBodySeverity(int temp)
    {
        int sign = CSMath.getSign(temp);
        int absTemp = Math.abs(temp);

        return
          absTemp < 100 ? (int) Math.floor(CSMath.blend(0, 3, absTemp, 0, 100)) * sign
        : (int) CSMath.blend(3, 7, absTemp, 100, 150) * sign;
    }

    public static void setBodyTemp(double temp)
    {
        BODY_TEMP = temp;
        PREV_BODY_TEMP = temp;
        BLEND_BODY_TEMP = (int) temp;
    }
}
