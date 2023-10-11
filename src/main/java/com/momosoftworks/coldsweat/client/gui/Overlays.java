package com.momosoftworks.coldsweat.client.gui;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.event.EntityTempManager;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.properties.IEntityTempProperty;
import com.momosoftworks.coldsweat.core.properties.PlayerTempProperty;
import com.momosoftworks.coldsweat.util.math.CSMath;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

public class Overlays extends Gui
{
    // Stuff for world temperature
    private static double WORLD_TEMP = 0;
    private static boolean ADVANCED_WORLD_TEMP = false;
    private static double PREV_WORLD_TEMP = 0;
    private static double MAX_OFFSET = 0;
    private static double MIN_OFFSET = 0;

    // Stuff for body temperature
    public static double BODY_TEMP = 0;
    static double PREV_BODY_TEMP = 0;
    static int BLEND_BODY_TEMP = 0;
    static int ICON_BOB = 0;
    static int BODY_ICON = 0;
    static int PREV_BODY_ICON = 0;
    static int BODY_TRANSITION_PROGRESS = 0;
    static int BODY_BLEND_TIME = 10;
    static int BODY_TEMP_SEVERITY = 0;

    static final ResourceLocation WORLD_TEMP_GAUGE = new ResourceLocation("cold_sweat:textures/gui/overlay/world_temp_gauge.png");
    static final ResourceLocation BODY_TEMP_GAUGE = new ResourceLocation("cold_sweat:textures/gui/overlay/body_temp_gauge.png");
    static final ResourceLocation VAGUE_TEMP_GAUGE = new ResourceLocation("cold_sweat:textures/gui/overlay/vague_temp_gauge.png");

    @SubscribeEvent
    public void onWorldTempRender(RenderGameOverlayEvent.Post event)
    {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL)
        {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.thePlayer;
            int width = event.resolution.getScaledWidth();
            int height = event.resolution.getScaledHeight();

            if (player != null && ADVANCED_WORLD_TEMP && !mc.gameSettings.hideGUI)
            {
                double min = ConfigSettings.MIN_TEMP.get();
                double max = ConfigSettings.MAX_TEMP.get();

                // Get player world temperature
                double temp = Temperature.convertUnits(WORLD_TEMP, ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);

                // Get the temperature severity
                int severity = getWorldSeverity(temp, min, max, MIN_OFFSET, MAX_OFFSET);

                // Set text color
                int color;
                switch (severity)
                {   case  2 : case 3 : color = 16297781; break;
                    case  4 : color = 16728089; break;
                    case -2 : case -3 : color = 8443135; break;
                    case -4 : color = 4236031; break;
                    default : color = 14737376; break;
                }

                /* Render gauge */

                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1, 1, 1, 1);

                // Set gauge texture
                mc.getTextureManager().bindTexture(WORLD_TEMP_GAUGE);

                // Render frame
                int gaugeX = (width / 2) + 92 + ConfigSettings.WORLD_GAUGE_OFFSET.get()[0];
                int gaugeY = height - 19 + ConfigSettings.WORLD_GAUGE_OFFSET.get()[1];
                func_146110_a(gaugeX, gaugeY, 0, 64 - severity * 16f, 25, 16, 25, 144);

                // Sets the text bobbing offset (or none if disabled)
                int bob = ClientSettingsConfig.iconBobbing && !CSMath.withinRange(temp, min + MIN_OFFSET, max + MAX_OFFSET) && player.ticksExisted % 2 == 0 ? 1 : 0;

                // Render text
                int blendedTemp = (int) CSMath.blend(PREV_WORLD_TEMP, WORLD_TEMP, event.partialTicks, 0, 1);

                String worldTempNum = (blendedTemp + ClientSettingsConfig.tempOffset)+"";
                Minecraft.getMinecraft().fontRenderer.drawString(worldTempNum,
                                gaugeX + 13 - Minecraft.getMinecraft().fontRenderer.getStringWidth(worldTempNum) / 2,
                                gaugeY + 4 - bob, color);
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
    }

    @SubscribeEvent
    public void onBodyTempRender(RenderGameOverlayEvent.Post event)
    {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL)
        {
            Minecraft mc = Minecraft.getMinecraft();
            int width = event.resolution.getScaledWidth();
            int height = event.resolution.getScaledHeight();

            if (!mc.thePlayer.capabilities.isCreativeMode && !Minecraft.getMinecraft().gameSettings.hideGUI)
            {
                // Blend body temp (per frame)
                BLEND_BODY_TEMP = (int) CSMath.blend(PREV_BODY_TEMP, BODY_TEMP, event.partialTicks, 0, 1);

                // Get text color
                int color;
                switch (BODY_TEMP_SEVERITY)
                {   case  7 : case -7 : color = 16777215; break;
                    case  6 : color = 16777132; break;
                    case  5 : color = 16767856; break;
                    case  4 : color = 16759634; break;
                    case  3 : color = 16751174; break;
                    case -3 : color = 6078975;  break;
                    case -4 : color = 7528447;  break;
                    case -5 : color = 8713471;  break;
                    case -6 : color = 11599871; break;
                    default : color = BLEND_BODY_TEMP > 0 ? 16744509
                             : BLEND_BODY_TEMP < 0 ? 4233468
                             : 11513775; break;
                }

                // Get the outer border color when readout is > 100
                int colorBG = BLEND_BODY_TEMP < 0 ? 1122643
                            : BLEND_BODY_TEMP > 0 ? 5376516
                            : 0;

                int bobLevel = Math.min(Math.abs(BODY_TEMP_SEVERITY), 3);
                int threatOffset = ClientSettingsConfig.iconBobbing ? 0
                                 : bobLevel == 2 ? ICON_BOB
                                 : bobLevel == 3 ? mc.thePlayer.ticksExisted % 2
                                 : 0;

                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                // Render old icon (if blending)
                mc.getTextureManager().bindTexture(BODY_TEMP_GAUGE);
                if (BODY_TRANSITION_PROGRESS < BODY_BLEND_TIME)
                {   func_146110_a((width / 2) - 5 + ConfigSettings.BODY_ICON_OFFSET.get()[0], height - 53 - threatOffset + ConfigSettings.BODY_ICON_OFFSET.get()[1], 0, 30 - PREV_BODY_ICON * 10, 10, 10, 10, 70);
                    GL11.glColor4f(1, 1, 1, (event.partialTicks + BODY_TRANSITION_PROGRESS) / BODY_BLEND_TIME);
                }

                // Render new icon on top of old icon (if blending)
                // Otherwise this is just the regular icon
                func_146110_a((width / 2) - 5 + ConfigSettings.BODY_ICON_OFFSET.get()[0], height - 53 - threatOffset + ConfigSettings.BODY_ICON_OFFSET.get()[1], 0, 30 - BODY_ICON * 10, 10, 10, 10, 70);
                GL11.glColor4f(1, 1, 1, 1);

                // Render Readout
                FontRenderer font = mc.fontRenderer;
                int scaledWidth = event.resolution.getScaledWidth();
                int scaledHeight = event.resolution.getScaledHeight();

                String s = "" + Math.min(Math.abs(BLEND_BODY_TEMP), 100);
                int x = (scaledWidth - font.getStringWidth(s)) / 2 + ConfigSettings.BODY_READOUT_OFFSET.get()[0];
                int y = scaledHeight - 31 - 10 + ConfigSettings.BODY_READOUT_OFFSET.get()[1];

                // Draw the outline
                font.drawString(s, x + 1, y, colorBG);
                font.drawString(s, x - 1, y, colorBG);
                font.drawString(s, x, y + 1, colorBG);
                font.drawString(s, x, y - 1, colorBG);

                // Draw the readout
                font.drawString(s, x, y, color);
            }
        }
    }

    @SubscribeEvent
    public void onVagueTempRender(RenderGameOverlayEvent.Post event)
    {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL)
        {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.thePlayer;
            int width = event.resolution.getScaledWidth();
            int height = event.resolution.getScaledHeight();

            if (player != null && !ADVANCED_WORLD_TEMP && !mc.gameSettings.hideGUI)
            {
                double min = ConfigSettings.MIN_TEMP.get();
                double max = ConfigSettings.MAX_TEMP.get();

                // Get player world temperature
                double temp = Temperature.convertUnits(WORLD_TEMP, ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);
                // Get the temperature severity
                int severity = getWorldSeverity(temp, min, max, MIN_OFFSET, MAX_OFFSET);
                int renderOffset = CSMath.clamp(severity, -1, 1) * 3;

                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1, 1, 1, 1);

                // Set gauge texture
                mc.getTextureManager().bindTexture(VAGUE_TEMP_GAUGE);

                // Render gauge
                func_146110_a((width / 2) + 96 + ConfigSettings.WORLD_GAUGE_OFFSET.get()[0], height - 19 + ConfigSettings.WORLD_GAUGE_OFFSET.get()[1] - renderOffset, 0, 64 - severity * 16, 16, 16, 16, 144);
            }
        }
    }

    // Handle temperature blending and transitions
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (event.phase == TickEvent.Phase.START && player != null)
        {
            IEntityTempProperty icap = EntityTempManager.getTemperatureProperty(player);
            if (!(icap instanceof PlayerTempProperty)) return;
            PlayerTempProperty cap = (PlayerTempProperty) icap;

            cap.calculateVisibility(player);
            ADVANCED_WORLD_TEMP = cap.showAdvancedWorldTemp();


            /* World Temp */

            // Get temperature in actual degrees
            boolean celsius = ConfigSettings.CELSIUS.get();
            double worldTemp = cap.getTemp(Temperature.Type.WORLD);
            double realTemp = Temperature.convertUnits(worldTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true);
            // Calculate the blended world temp for this tick
            double diff = realTemp - WORLD_TEMP;
            PREV_WORLD_TEMP = WORLD_TEMP;
            WORLD_TEMP += Math.abs(diff) <= 0.5 ? diff : diff / 4d;

            // Update max/min offset
            MAX_OFFSET = cap.getTemp(Temperature.Type.FREEZING_POINT);
            MIN_OFFSET = cap.getTemp(Temperature.Type.BURNING_POINT);


            /* Body Temp */

            // Blend body temp (per tick)
            PREV_BODY_TEMP = BODY_TEMP;
            double currentTemp = cap.getTemp(Temperature.Type.BODY);
            BODY_TEMP = Math.abs(currentTemp - BODY_TEMP) < 0.1 ? currentTemp : BODY_TEMP + (cap.getTemp(Temperature.Type.BODY) - BODY_TEMP) / 5;

            // Handle effects for the icon (bobbing, stage, transition)
            // Get icon bob
            ICON_BOB = player.ticksExisted % 3 == 0 && Math.random() < 0.3 ? 1 : 0;

            // Get the severity of the player's body temperature
            BODY_TEMP_SEVERITY = getBodySeverity(BLEND_BODY_TEMP);

            // Get the icon to be displayed
            int neededIcon = CSMath.clamp(BODY_TEMP_SEVERITY, -3, 3);

            // Start transition
            if (BODY_ICON != neededIcon)
            {   BODY_ICON = neededIcon;
                BODY_TRANSITION_PROGRESS = 0;
            }

            // Tick the transition progress
            if (PREV_BODY_ICON != BODY_ICON && BODY_TRANSITION_PROGRESS++ >= BODY_BLEND_TIME)
            {   PREV_BODY_ICON = BODY_ICON;
            }
        }
    }

    public static int getWorldSeverity(double temp, double min, double max, double offsMin, double offsMax)
    {   return (int) CSMath.blend(-4, 4, temp, min + offsMin, max + offsMax);
    }

    static int getBodySeverity(int temp)
    {
        int sign = CSMath.getSign(temp);
        int absTemp = Math.abs(temp);

        return absTemp < 100 ? (int) Math.floor(CSMath.blend(0, 3, absTemp, 0, 100)) * sign
                             : (int) CSMath.blend(3, 7, absTemp, 100, 150) * sign;
    }

    public static void setBodyTemp(double temp)
    {   BODY_TEMP = temp;
        PREV_BODY_TEMP = temp;
        BLEND_BODY_TEMP = (int) temp;
    }

    public static double getWorldTemp()
    {   return WORLD_TEMP;
    }

    public static double getBodyTemp()
    {   return BODY_TEMP;
    }

    public static double getMinOffset()
    {   return MIN_OFFSET;
    }

    public static double getMaxOffset()
    {   return MAX_OFFSET;
    }
}
