package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.api.temperature.modifier.FoodTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.PlayerTempCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Overlays
{
    public static final ResourceLocation BODY_TEMP_GAUGE = new ResourceLocation("cold_sweat:textures/gui/overlay/body_temp_gauge.png");
    public static final ResourceLocation BODY_TEMP_GAUGE_HC = new ResourceLocation("cold_sweat:textures/gui/overlay/body_temp_gauge_hc.png");
    public static final ResourceLocation WORLD_TEMP_GAUGE = new ResourceLocation("cold_sweat:textures/gui/overlay/world_temp_gauge.png");
    public static final ResourceLocation WORLD_TEMP_GAUGE_HC = new ResourceLocation("cold_sweat:textures/gui/overlay/world_temp_gauge_hc.png");
    public static final ResourceLocation VAGUE_TEMP_GAUGE = new ResourceLocation("cold_sweat:textures/gui/overlay/vague_temp_gauge.png");
    public static final ResourceLocation VAGUE_TEMP_GAUGE_HC = new ResourceLocation("cold_sweat:textures/gui/overlay/vague_temp_gauge_hc.png");
    public static final ResourceLocation VAGUE_TEMP_GAUGE_STANDALONE = new ResourceLocation("cold_sweat:textures/gui/overlay/vague_temp_gauge_standalone.png");
    public static final ResourceLocation VAGUE_TEMP_GAUGE_STANDALONE_HC = new ResourceLocation("cold_sweat:textures/gui/overlay/vague_temp_gauge_standalone_hc.png");
    public static final ResourceLocation FOOD_EFFECT = new ResourceLocation("cold_sweat:textures/gui/overlay/food_effect_background.png");

    public static final Supplier<ResourceLocation> BODY_TEMP_GAUGE_LOCATION  = () ->
            ConfigSettings.HIGH_CONTRAST.get() ? BODY_TEMP_GAUGE_HC
                                               : BODY_TEMP_GAUGE;
    public static final Supplier<ResourceLocation> WORLD_TEMP_GAUGE_LOCATION = () ->
            ConfigSettings.HIGH_CONTRAST.get() ? WORLD_TEMP_GAUGE_HC
                                               : WORLD_TEMP_GAUGE;
    public static final Supplier<ResourceLocation> VAGUE_TEMP_GAUGE_LOCATION = () ->
            ConfigSettings.BODY_ICON_ENABLED.get()
            ? ConfigSettings.HIGH_CONTRAST.get() ? VAGUE_TEMP_GAUGE_HC : VAGUE_TEMP_GAUGE
            : ConfigSettings.HIGH_CONTRAST.get() ? VAGUE_TEMP_GAUGE_STANDALONE_HC: VAGUE_TEMP_GAUGE_STANDALONE;

    // Stuff for world temperature
    static boolean ADVANCED_WORLD_TEMP = false;
    public static double WORLD_TEMP = 0;
    static double PREV_WORLD_TEMP = 0;
    static double PLAYER_MAX_TEMP = 0;
    static double PLAYER_MIN_TEMP = 0;

    // Stuff for body temperature
    static double BODY_TEMP = 0;
    static double PREV_BODY_TEMP = 0;
    public static double BLEND_BODY_TEMP = 0;
    static int ICON_BOB = 0;
    static int BODY_ICON = 0;
    static int PREV_BODY_ICON = 0;
    static double BODY_TEMP_SEVERITY = 0;

    public static IGuiOverlay WORLD_TEMP_OVERLAY = (gui, graphics, partialTick, width, height) ->
    {
        PoseStack poseStack = graphics.pose();
        Font font = Minecraft.getInstance().font;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && ADVANCED_WORLD_TEMP
        && Minecraft.getInstance().gameMode.getPlayerMode() != GameType.SPECTATOR
        && !Minecraft.getInstance().options.hideGui && ConfigSettings.WORLD_GAUGE_ENABLED.get())
        {
            gui.setupOverlayRenderState(true, false);

            // Get player world temperature
            double temp = Temperature.convert(WORLD_TEMP, ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);

            // Get the temperature severity
            int severity = getGaugeSeverity(temp, PLAYER_MIN_TEMP, PLAYER_MAX_TEMP);

            // Set text color
            int color = switch (severity)
            {   case  2, 3 -> 16297781;
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

            // Render frame
            graphics.blit(WORLD_TEMP_GAUGE_LOCATION.get(),
                          (width / 2) + 93 + ConfigSettings.WORLD_GAUGE_POS.get().x(),
                          height - 19 + ConfigSettings.WORLD_GAUGE_POS.get().y(), 0, 64 - severity * 16, 25, 16, 25, 144);

            RenderSystem.disableBlend();

            // Sets the text bobbing offset (or none if disabled)
            int bob = ConfigSettings.ICON_BOBBING.get() && !CSMath.betweenInclusive(temp, PLAYER_MIN_TEMP, PLAYER_MAX_TEMP) && player.tickCount % 2 == 0 ? 1 : 0;

            // Render text
            int blendedTemp = (int) CSMath.blend(PREV_WORLD_TEMP, WORLD_TEMP, Minecraft.getInstance().getFrameTime(), 0, 1);

            graphics.drawString(font, (blendedTemp + ConfigSettings.TEMP_OFFSET.get())+"",
                    /* X */ width / 2 + 106 + (Integer.toString(blendedTemp + ConfigSettings.TEMP_OFFSET.get()).length() * -3) + ConfigSettings.WORLD_GAUGE_POS.get().x(),
                    /* Y */ height - 15 - bob + ConfigSettings.WORLD_GAUGE_POS.get().y(), color, false);
            poseStack.popPose();
        }
    };

    public static IGuiOverlay BODY_TEMP_OVERLAY = (gui, graphics, partialTick, width, height) ->
    {
        gui.setupOverlayRenderState(true, false);
        Minecraft mc = Minecraft.getInstance();

        // Blend body temperature (per frame)
        BLEND_BODY_TEMP = CSMath.blend(PREV_BODY_TEMP, BODY_TEMP, Minecraft.getInstance().getPartialTick(), 0, 1);
        double bodyTempInt = CSMath.roundNearest(BLEND_BODY_TEMP, 1);

        if (gui.shouldDrawSurvivalElements() && !Minecraft.getInstance().options.hideGui)
        {
            // Get text color
            int color = bodyTempInt > 0 ? 16744509
                      : bodyTempInt < 0 ? 4233468
                      : 11513775;

            // Get the outer border color when readout is > 100
            int colorBG = bodyTempInt < 0 ? 1122643
                        : bodyTempInt > 0 ? 5376516
                        : 0;

            int bobLevel = Math.min(Math.abs(((int) BODY_TEMP_SEVERITY)), 3);
            int threatOffset = !ConfigSettings.ICON_BOBBING.get() ? 0
                             : bobLevel == 2 ? ICON_BOB
                             : bobLevel == 3 ? Minecraft.getInstance().cameraEntity.tickCount % 2
                             : 0;

            RenderSystem.defaultBlendFunc();

            // Render old icon (if blending)
            if (ConfigSettings.BODY_ICON_ENABLED.get())
            {
                int icon = Math.abs(bodyTempInt) < 100 ?  CSMath.floor(BODY_TEMP_SEVERITY) : 4 * CSMath.sign(BODY_TEMP_SEVERITY);
                int iconX = (width / 2) - 5 + ConfigSettings.BODY_ICON_POS.get().x();
                int iconYOffset = ADVANCED_WORLD_TEMP && ConfigSettings.MOVE_BODY_ICON_WHEN_ADVANCED.get()
                                  ? 54
                                  : 47;
                int iconY = height - iconYOffset - threatOffset + ConfigSettings.BODY_ICON_POS.get().y();
                // Render icon
                graphics.blit(BODY_TEMP_GAUGE_LOCATION.get(), iconX, iconY, 0, 40 - icon * 10, 10, 10, 10, 90);

                // Render new icon if temperature changing
                if (CSMath.betweenExclusive(Math.abs(bodyTempInt), 0, 100))
                {
                    // render new icon over old icon
                    double blend = CSMath.blend(1, 9, Math.abs(BODY_TEMP_SEVERITY), Math.abs(CSMath.floor(BODY_TEMP_SEVERITY)), Math.abs(CSMath.ceil(BODY_TEMP_SEVERITY)));
                    graphics.blit(BODY_TEMP_GAUGE_LOCATION.get(),
                                  iconX, iconY + 10 - CSMath.ceil(blend), 0,
                                  // UV Y-coordinate for the icon in this stage
                                  40 - CSMath.grow(icon, bodyTempInt > 0 ? 0 : 2) * 10 - CSMath.ceil(blend),
                                  10, CSMath.ceil(blend), 10, 90);
                }
            }

            // Render Readout
            if (ConfigSettings.BODY_READOUT_ENABLED.get() && ADVANCED_WORLD_TEMP)
            {
                Font font = mc.font;
                int scaledWidth = mc.getWindow().getGuiScaledWidth();
                int scaledHeight = mc.getWindow().getGuiScaledHeight();

                String s = "" + (int) Math.min(Math.abs(bodyTempInt), 100);
                int x = (scaledWidth - font.width(s)) / 2 + ConfigSettings.BODY_READOUT_POS.get().x();
                int y = scaledHeight - 31 - 10 + ConfigSettings.BODY_READOUT_POS.get().y();

                // Draw the outline
                graphics.drawString(font, s, x + 1, y, colorBG, false);
                graphics.drawString(font, s, x - 1, y, colorBG, false);
                graphics.drawString(font, s, x, y + 1, colorBG, false);
                graphics.drawString(font, s, x, y - 1, colorBG, false);

                // Draw the readout
                graphics.drawString(font, s, x, y, color, false);

                // Render white overlay if temp is > 100
                if (Math.abs(bodyTempInt) > 100)
                {
                    // Calculate the height of the white overlay
                    int textHeight = font.wordWrapHeight(s, 100);
                    int overlayHeight = (int) CSMath.roundUpNearest(CSMath.blend(2, textHeight, Math.abs(bodyTempInt), 100, 150), 1);
                    // Overlay color
                    int overlayColor = overlayHeight > 3 ? bodyTempInt > 0 ? 16777132 : 11599871
                                     : bodyTempInt > 0 ? 16771975 : 8713471;
                    int overlayColor2 = bodyTempInt > 0 ? 16759634 : 7528447;

                    ClientOnlyHelper.renderVerticalCropText(s, x, y, Math.min(textHeight, overlayHeight + 1), overlayColor2, graphics);
                    ClientOnlyHelper.renderVerticalCropText(s, x, y, overlayHeight, overlayColor, graphics);
                }
            }
        }
    };

    public static IGuiOverlay VAGUE_TEMP_OVERLAY = (gui, graphics, partialTick, width, height) ->
    {
        PoseStack poseStack = graphics.pose();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null && !ADVANCED_WORLD_TEMP && mc.gameMode.getPlayerMode() != GameType.SPECTATOR
        && !mc.options.hideGui && ConfigSettings.WORLD_GAUGE_ENABLED.get() && gui.shouldDrawSurvivalElements())
        {
            gui.setupOverlayRenderState(true, false);

            // Get player world temperature
            double temp = Temperature.convert(WORLD_TEMP, ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);
            // Get the temperature severity
            int severity = getGaugeSeverity(temp, PLAYER_MIN_TEMP, PLAYER_MAX_TEMP);
            int renderOffset = CSMath.clamp(severity, -1, 1) * 2;

            poseStack.pushPose();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            int bobLevel = Math.min(Math.abs(((int) BODY_TEMP_SEVERITY)), 3);
            int threatOffset = !ConfigSettings.ICON_BOBBING.get()
                               ? 0
                               : bobLevel == 2
                                 ? ICON_BOB
                                 : bobLevel == 3
                                   ? Minecraft.getInstance().cameraEntity.tickCount % 2
                                   : 0;

            // Render frame
            graphics.blit(VAGUE_TEMP_GAUGE_LOCATION.get(),
                          (width / 2) - 8 + ConfigSettings.BODY_ICON_POS.get().x(),
                          height - 50 + ConfigSettings.BODY_ICON_POS.get().y() - renderOffset - threatOffset,
                          0, 64 - severity * 16, 16, 16, 16, 144);

            RenderSystem.disableBlend();
            poseStack.popPose();
        }
    };

    public static IGuiOverlay FOOD_EFFECTS_OVERLAY = (gui, graphics, partialTick, width, height) ->
    {
        if (!ConfigSettings.FOOD_EFFECTS_ENABLED.get()) return;

        gui.setupOverlayRenderState(true, false);
        RenderSystem.enableBlend();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        graphics.pose().pushPose();
        Vector2i pos = ConfigSettings.FOOD_EFFECTS_POS.get();
        graphics.pose().translate(pos.x, pos.y, 0);
        int offset = 0;
        for (Map.Entry<Temperature.Trait, List<TempModifier>> entry : Temperature.getModifiers(player).entrySet())
        {
            Temperature.Trait trait = entry.getKey();
            List<TempModifier> modifierList = new ArrayList<>(entry.getValue());
            List<FoodTempModifier> sortedModifiers = modifierList.stream()
                                                     .filter(mod -> mod instanceof FoodTempModifier)
                                                     .map(mod -> (FoodTempModifier) mod)
                                                     // Sort by absolute value, positive temps first
                                                     .sorted((m1, m2) -> {
                                                         int sign1 = CSMath.sign(m1.getNBT().getDouble("temperature"));
                                                         int sign2 = CSMath.sign(m2.getNBT().getDouble("temperature"));
                                                         if (sign1 != sign2) return Integer.compare(sign1, sign2);
                                                         return Double.compare(Math.abs(m1.getNBT().getDouble("temperature")), Math.abs(m2.getNBT().getDouble("temperature")));
                                                     }).toList();
            for (TempModifier modifier : sortedModifiers)
            {
                if (modifier instanceof FoodTempModifier food)
                {
                    int x = width - 10;
                    int y = height - 10 - offset;

                    if (food.getNBT().getDouble("duration") == 0) continue;
                    int timeLeft = food.getExpireTime() - food.getTicksExisted();
                    double temp = food.getNBT().getDouble("temperature");
                    boolean overridden = food.isOverridden(trait);

                    // Render background
                    // background is 76x24
                    graphics.blit(FOOD_EFFECT, x - 76, y - 24, 0, 0, 76, 24, 76, 24);

                    String sign = temp > 0 ? "↑" : "↓";
                    String tempString = CSMath.formatDoubleOrInt(CSMath.round(temp, 2));
                    if (temp < 0) tempString = tempString.substring(1);
                    tempString = sign + tempString;
                    String timerString = StringUtil.formatTickDuration(timeLeft);
                    int contentWidth = mc.font.width(tempString) + mc.font.width(timerString) + 20;
                    int contentHeight = 16;
                    x -= 76;

                    graphics.pose().pushPose();
                    graphics.pose().translate(76/2 - contentWidth / 2, 24/2 - contentHeight / 2, 0);
                    float brightness = overridden ? 0.35f : 1f;
                    RenderSystem.setShaderColor(brightness, brightness, brightness, 1);

                    // Draw timer
                    if (timeLeft < 200)
                    {
                        float alpha = (float) CSMath.blend(1, Math.sin((food.getTicksExisted()+partialTick) / 1.5) / 2 + 0.5, timeLeft, 200, 0);
                        RenderSystem.setShaderColor(brightness, brightness, brightness, alpha);
                    }
                    graphics.drawString(mc.font, timerString, x, y - mc.font.lineHeight - 11, ChatFormatting.WHITE.getColor(), true);
                    RenderSystem.setShaderColor(brightness, brightness, brightness, 1);
                    x += mc.font.width(timerString) + 2;

                    // Draw item
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(food.getNBT().getString("item")));
                    graphics.renderItem(item.getDefaultInstance(), x, y - 24);
                    x += 16 + 2;

                    // Draw temperature text
                    Style style = temp > 0 ? TooltipHandler.HOT : temp < 0 ? TooltipHandler.COLD : Style.EMPTY;
                    Component tempText = Component.literal(tempString).withStyle(style);

                    graphics.drawString(mc.font, tempText, x, y - mc.font.lineHeight - 11, tempText.getStyle().getColor().getValue(), true);

                    offset += 25;
                    graphics.pose().popPose();
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                }
            }
        }
        graphics.pose().popPose();
    };

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event)
    {   event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "world_temp", WORLD_TEMP_OVERLAY);
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "body_temp", BODY_TEMP_OVERLAY);
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "vague_temp", VAGUE_TEMP_OVERLAY);
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "food_effects", FOOD_EFFECTS_OVERLAY);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static final class TickOverlays
    {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event)
        {
            Player player = Minecraft.getInstance().player;
            if (event.phase == TickEvent.Phase.START && player != null && player.isAlive())
            {
                EntityTempManager.getTemperatureCap(player).ifPresent(icap ->
                {
                    if (!(icap instanceof PlayerTempCap cap)) return;

                    cap.calculateHudVisibility(player);
                    ADVANCED_WORLD_TEMP = cap.showAdvancedWorldTemp();

                    /* World Temp */

                    // Get temperature in actual degrees
                    boolean celsius = ConfigSettings.CELSIUS.get();
                    double worldTemp = cap.getTrait(Temperature.Trait.WORLD);
                    double realTemp = Temperature.convert(worldTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true);
                    // Calculate the blended world temperature for this tick
                    double diff = realTemp - WORLD_TEMP;
                    PREV_WORLD_TEMP = WORLD_TEMP;
                    WORLD_TEMP += Math.abs(diff) <= 1 ? diff : CSMath.maxAbs(diff / ConfigSettings.TEMP_SMOOTHING.get(), 0.25 * CSMath.sign(diff));

                    // Update max/min temps
                    PLAYER_MAX_TEMP = cap.getTrait(Temperature.Trait.BURNING_POINT);
                    PLAYER_MIN_TEMP = cap.getTrait(Temperature.Trait.FREEZING_POINT);

                    /* Body Temp */

                    // Blend body temp (per tick)
                    PREV_BODY_TEMP = BODY_TEMP;
                    BODY_TEMP = BODY_TEMP + (cap.getTrait(Temperature.Trait.BODY) - BODY_TEMP) / 2;

                    // Handle effects for the icon (bobbing, stage, transition)
                    // Get icon bob
                    ICON_BOB = player.tickCount % 3 == 0 && Math.random() < 0.3 ? 1 : 0;

                    // Get the severity of the player's body temperature
                    BODY_TEMP_SEVERITY = getBodySeverity((int) BLEND_BODY_TEMP);

                    // Get the icon to be displayed
                    int neededIcon = ((int) CSMath.clamp(BODY_TEMP_SEVERITY, -4, 4));

                    // Start transition
                    if (BODY_ICON != neededIcon)
                    {   BODY_ICON = neededIcon;
                    }
                });
            }
        }
    }

    /**
     * Gets the given temperature's severity, relative to the player's min and max temperatures.
     * @param playerMin The player's minimum temperature
     * @param playerMax The player's maximum temperature
     * @return A number between -1 and 1, representing the severity of the temperature
     */
    public static double getWorldSeverity(double temp, double playerMin, double playerMax)
    {
        if (temp < playerMin) return -1;
        if (temp > playerMax) return 1;

        double normalMin = ConfigSettings.Difficulty.NORMAL.getSetting(ConfigSettings.MIN_TEMP);
        double normalMax = ConfigSettings.Difficulty.NORMAL.getSetting(ConfigSettings.MAX_TEMP);

        double mid = calculateMidpoint(playerMin, playerMax, normalMin, normalMax);

        return (temp < mid)
               ? CSMath.blend(-1, 0, temp, playerMin, mid)
               : CSMath.blend(0, 1, temp, mid, playerMax);
    }

    private static double calculateMidpoint(double playerMin, double playerMax, double normalMin, double normalMax)
    {
        boolean bothBelowNormal = playerMin < normalMin && playerMax < normalMax;
        boolean bothAboveNormal = playerMin > normalMin && playerMax > normalMax;

        return (bothBelowNormal || bothAboveNormal)
               ? (playerMin + playerMax) / 2
               : (normalMin + normalMax) / 2;
    }

    public static int getGaugeSeverity(double temp, double playerMin, double playerMax)
    {   return (int) (getWorldSeverity(temp, playerMin, playerMax) * 4);
    }

    static double getBodySeverity(int temp)
    {   int sign = CSMath.sign(temp);
        int absTemp = Math.abs(temp);

        return absTemp < 100 ? CSMath.blend(0d, 3d, absTemp, 0, 100) * sign
                             : CSMath.blend(3d, 7d, absTemp, 100, 150) * sign;
    }

    public static void setBodyTempInstant(double temp)
    {   BODY_TEMP = temp;
        PREV_BODY_TEMP = temp;
        BLEND_BODY_TEMP = (int) temp;
    }

    public static void setWorldTempInstant(double temp)
    {   WORLD_TEMP = temp;
        PREV_WORLD_TEMP = temp;
    }
}
