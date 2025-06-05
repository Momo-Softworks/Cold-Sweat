package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import static com.momosoftworks.coldsweat.common.event.HandleTempEffects.Client.COLD_IMMUNITY;

public class FreezeVignetteEffect extends TempEffect
{
    public FreezeVignetteEffect(LivingEntity entity, IntegerBounds range)
    {   super(entity, range);
    }

    static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");

    protected void setupRender(float opacity)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, opacity);
        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void vignette(RenderGuiLayerEvent.Pre event)
    {
        Player player = Minecraft.getInstance().player;
        if (!this.test(player)) return;
        if (HandleTempEffects.isPlayerImmune(player)) return;

        float blendTemp = (float) Overlays.BLEND_BODY_TEMP;

        if (event.getName() == VanillaGuiLayers.CAMERA_OVERLAYS && blendTemp < 0 && COLD_IMMUNITY < 1)
        {
            // Setup calculations
            float resistance = (float) CSMath.blend(1, 0, COLD_IMMUNITY, 0, 1);
            float opacity = CSMath.blend(0f, 1f, blendTemp, this.bounds().min(), this.bounds().max()) * resistance;
            if (opacity == 0) return;
            float width = Minecraft.getInstance().getWindow().getWidth();
            float height = Minecraft.getInstance().getWindow().getHeight();
            float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // Set up shader and texture
            this.setupRender(opacity);
            // Render vignette
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.addVertex(0.0f, height / scale, -90.0f).setUv(0.0F, 1.0F);
            bufferbuilder.addVertex(width / scale, height / scale, -90.0f).setUv(1.0F, 1.0F);
            bufferbuilder.addVertex(width / scale, 0.0f, -90.0f).setUv(1.0F, 0.0F);
            bufferbuilder.addVertex(0.0f, 0.0f, -90.0f).setUv(0.0F, 0.0F);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
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
