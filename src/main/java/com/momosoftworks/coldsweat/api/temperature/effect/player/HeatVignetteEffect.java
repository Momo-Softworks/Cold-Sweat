package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.event.HandleTempEffects;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.momosoftworks.coldsweat.common.event.HandleTempEffects.Client.HOT_IMMUNITY;

public class HeatVignetteEffect extends TempEffect
{
    public HeatVignetteEffect(LivingEntity entity, IntegerBounds range)
    {   super(entity, range);
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/overlay/haze.png");

    protected void setupRender(float opacity, float tickTime)
    {
        float vignetteBrightness = opacity + ((float) Math.sin((tickTime + 3) / (Math.PI * 1.0132f)) / 5f - 0.2f) * opacity;
        RenderSystem.setShaderColor(0.231f, 0f, 0f, vignetteBrightness);
        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void vignette(RenderGuiOverlayEvent.Pre event)
    {
        Player player = Minecraft.getInstance().player;
        if (!this.test(player)) return;
        if (HandleTempEffects.isPlayerImmune(player)) return;

        float blendTemp = (float) Overlays.BLEND_BODY_TEMP;

        if (event.getOverlay() == VanillaGuiOverlay.VIGNETTE.type() && blendTemp > 0 && HOT_IMMUNITY < 1)
        {
            // Setup calculations
            float resistance = (float) CSMath.blend(1, 0, HOT_IMMUNITY, 0, 1);
            float opacity = CSMath.blend(0f, 1f, blendTemp, this.bounds().min(), this.bounds().max()) * resistance;
            float tickTime = player.tickCount + event.getPartialTick();
            if (opacity == 0) return;
            double width = event.getWindow().getWidth();
            double height = event.getWindow().getHeight();
            double scale = event.getWindow().getGuiScale();

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // Set up shader and texture
            this.setupRender(opacity, tickTime);
            // Render vignette
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(0.0D, height / scale, -90.0D).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(width / scale, height / scale, -90.0D).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(width / scale, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
            tesselator.end();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
        }
    }

    @Override
    public boolean isClient()
    {   return true;
    }
}
