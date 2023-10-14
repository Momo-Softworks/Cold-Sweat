package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.config.ColdSweatConfig;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraft.client.renderer.Tessellator;

import java.lang.reflect.Field;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TempEffectsClient
{
    static float BLEND_TEMP = 0;

    static float PREV_X_SWAY = 0;
    static float PREV_Y_SWAY = 0;
    static float X_SWAY_SPEED = 0;
    static float Y_SWAY_SPEED = 0;
    static float X_SWAY_PHASE = 0;
    static float Y_SWAY_PHASE = 0;
    static float TIME_SINCE_NEW_SWAY = 0;

    static int COLD_IMMUNITY = 0;
    static int HOT_IMMUNITY  = 0;

    // Sway the player's camera when the player is too hot; swaying is more drastic at higher temperatures
    @SubscribeEvent
    public static void setCamera(EntityViewRenderEvent.CameraSetup event)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if (!Minecraft.getInstance().isPaused() && player != null)
        {
            // Get the FPS of the game
            float frameTime = Minecraft.getInstance().getDeltaFrameTime();
            float temp = (float) Temperature.get(player, Temperature.Type.BODY);
            // Get a blended version of the player's temperature
            // More important for fog stuff
            BLEND_TEMP += (temp - BLEND_TEMP) * frameTime / 20;

            if (ClientSettingsConfig.getInstance().areDistortionsEnabled())
            {
                // Camera "shivers" when temp is < -50
                if (BLEND_TEMP <= -50 && COLD_IMMUNITY < 4)
                {
                    float factor = CSMath.blend(0.05f, 0f, BLEND_TEMP, -100, -50);
                    double tickTime = player.tickCount + event.getRenderPartialTicks();
                    float shiverAmount = (float) (Math.sin((tickTime) * 3) * factor * (10 * frameTime));
                    player.setYHeadRot(player.getYHeadRot() + shiverAmount);
                }
                else if (BLEND_TEMP >= 50 && HOT_IMMUNITY < 4)
                {
                    float immunityModifier = CSMath.blend(BLEND_TEMP, 50, HOT_IMMUNITY, 0, 4);
                    float factor = CSMath.blend(0, 8, immunityModifier, 50, 100);

                    // Set random sway speed every once in a while
                    if (TIME_SINCE_NEW_SWAY > 100 || X_SWAY_SPEED == 0 || Y_SWAY_SPEED == 0)
                    {
                        TIME_SINCE_NEW_SWAY = 0;
                        X_SWAY_SPEED = (float) (Math.random() * 0.01f + 0.01f);
                        Y_SWAY_SPEED = (float) (Math.random() * 0.01f + 0.01f);
                    }
                    TIME_SINCE_NEW_SWAY += frameTime;

                    // Blend to the new sway speed
                    X_SWAY_PHASE += 2 * Math.PI * frameTime * X_SWAY_SPEED;
                    Y_SWAY_PHASE += 2 * Math.PI * frameTime * Y_SWAY_SPEED;

                    // Apply the sway speed to a sin function
                    float xOffs = (float) (Math.sin(X_SWAY_PHASE) * factor);
                    float yOffs = (float) (Math.sin(Y_SWAY_PHASE) * factor);

                    // Apply the sway
                    player.xRot = player.xRot + xOffs - PREV_X_SWAY;
                    player.yRot = player.yRot + yOffs - PREV_Y_SWAY;

                    // Save the previous sway
                    PREV_X_SWAY = xOffs;
                    PREV_Y_SWAY = yOffs;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END)
        {
            PlayerEntity player = Minecraft.getInstance().player;
            if (player != null && player.tickCount % 5 == 0)
            {
                COLD_IMMUNITY = 0;
                HOT_IMMUNITY = 0;
                boolean hasGrace = player.hasEffect(ModEffects.GRACE);
                if (player.hasEffect(ModEffects.ICE_RESISTANCE) || hasGrace) COLD_IMMUNITY = 4;
                if (player.hasEffect(Effects.FIRE_RESISTANCE) || hasGrace) HOT_IMMUNITY = 4;

                if (CompatManager.isArmorUnderwearLoaded() && (COLD_IMMUNITY < 4 || HOT_IMMUNITY < 4))
                {   player.getArmorSlots().forEach(stack ->
                    {
                        if (CompatManager.hasOllieLiner(stack))
                            HOT_IMMUNITY++;
                        if (CompatManager.hasOttoLiner(stack))
                            COLD_IMMUNITY++;
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderFog(EntityViewRenderEvent event)
    {
        if (!(event instanceof EntityViewRenderEvent.RenderFogEvent || event instanceof EntityViewRenderEvent.FogColors)) return;

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null && BLEND_TEMP >= 50 && ColdSweatConfig.getInstance().heatstrokeFog() && HOT_IMMUNITY < 4)
        {
            float immunityModifier = CSMath.blend(BLEND_TEMP, 50, HOT_IMMUNITY, 0, 4);
            if (event instanceof EntityViewRenderEvent.RenderFogEvent)
            {
                EntityViewRenderEvent.FogDensity fog = (EntityViewRenderEvent.FogDensity) event;
                float density = CSMath.withinRange(immunityModifier, 50, 55)
                                ? CSMath.blend(-1, 0f, immunityModifier, 50f, 55f)
                                : CSMath.withinRange(immunityModifier, 55, 80)
                                ? CSMath.blend(0f, 0.1f, immunityModifier, 55f, 80f)
                                : CSMath.blend(0.1f, 0.3f, immunityModifier, 80f, 90f);
                ((EntityViewRenderEvent.FogDensity) event).setDensity(density);
                fog.setCanceled(true);
            }
            else
            {   EntityViewRenderEvent.FogColors fogColor = (EntityViewRenderEvent.FogColors) event;
                fogColor.setRed(CSMath.blend(fogColor.getRed(), 0.01f, immunityModifier, 50, 90));
                fogColor.setGreen(CSMath.blend(fogColor.getGreen(), 0.01f, immunityModifier, 50, 90));
                fogColor.setBlue(CSMath.blend(fogColor.getBlue(), 0.05f, immunityModifier, 50, 90));
            }
        }
    }

    static final ResourceLocation HAZE_TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/overlay/haze.png");
    static final ResourceLocation FREEZE_TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/overlay/freeze_overlay.png");

    @SubscribeEvent
    public static void vignette(RenderGameOverlayEvent.Pre event)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null && event.getType() == RenderGameOverlayEvent.ElementType.ALL
        && ((BLEND_TEMP > 0 && HOT_IMMUNITY < 4) || (BLEND_TEMP < 0 && COLD_IMMUNITY < 4)))
        {
            float tempWithImmunity = CSMath.blend(BLEND_TEMP, 50, BLEND_TEMP > 0 ? HOT_IMMUNITY : COLD_IMMUNITY, 0, 4);
            float opacity = CSMath.blend(0f, 1f, Math.abs(tempWithImmunity), 50, 100);
            float tickTime = player.tickCount + event.getPartialTicks();
            if (opacity == 0) return;
            double width = event.getWindow().getWidth();
            double height = event.getWindow().getHeight();
            double scale = event.getWindow().getGuiScale();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (tempWithImmunity > 0)
            {   float vignetteBrightness = opacity + ((float) Math.sin((tickTime + 3) / (Math.PI * 1.0132f)) / 5f - 0.2f) * opacity;
                RenderSystem.color4f(0.231f, 0f, 0f, vignetteBrightness);
                Minecraft.getInstance().textureManager.bind(HAZE_TEXTURE);
            }
            else
            {   RenderSystem.color4f(1f, 1f, 1f, opacity);
                Minecraft.getInstance().textureManager.bind(FREEZE_TEXTURE);
            }
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.vertex(0.0D, height / scale, -90.0D).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(width / scale, height / scale, -90.0D).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(width / scale, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
            tessellator.end();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
        }
    }

    static ShaderUniform BLUR_RADIUS = null;
    static Field POST_PASSES = null;
    static boolean BLUR_APPLIED = false;

    static
    {
        try
        {
            POST_PASSES = ObfuscationReflectionHelper.findField(ShaderGroup.class, "field_148031_d");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @SubscribeEvent
    public static void onRenderBlur(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
        {
            Minecraft mc = Minecraft.getInstance();
            try
            {
                float playerTemp = (float) Overlays.BODY_TEMP;
                if (ClientSettingsConfig.getInstance().areDistortionsEnabled() && playerTemp >= 50 && HOT_IMMUNITY < 4)
                {
                    float blur = CSMath.blend(0f, 7f, playerTemp, 50, 100) / (HOT_IMMUNITY + 1);
                    if (blur > 0 && (mc.gameRenderer.currentEffect() == null || !mc.gameRenderer.currentEffect().getName().equals("minecraft:shaders/post/blobs2.json")))
                    {   BLUR_APPLIED = false;
                    }
                    if (!BLUR_APPLIED)
                    {   mc.gameRenderer.loadEffect(new ResourceLocation("shaders/post/blobs2.json"));
                        BLUR_RADIUS = ((List<Shader>) POST_PASSES.get(mc.gameRenderer.currentEffect())).get(0).getEffect().getUniform("Radius");
                        BLUR_APPLIED = true;
                    }
                    if (BLUR_RADIUS != null)
                    {   BLUR_RADIUS.set(blur);
                    }
                }
                else if (BLUR_APPLIED)
                {   BLUR_RADIUS.set(0f);
                    BLUR_APPLIED = false;
                }
            } catch (Exception ignored) {}
        }
    }
}
