package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.joml.Vector4f;

public abstract class AbstractVignetteEffect extends TempEffect
{
    public AbstractVignetteEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    protected abstract ResourceLocation getTexture();
    protected abstract Vector4f getColor(float tickTime);

    protected void setupRender(float opacity, float tickTime)
    {
        Vector4f color = this.getColor(tickTime);
        RenderSystem.setShaderColor(color.x, color.y, color.z, opacity * color.w);
        RenderSystem.setShaderTexture(0, this.getTexture());
    }

    protected void render(float opacity, float tickTime, RenderGuiLayerEvent.Pre event)
    {
        float width = Minecraft.getInstance().getWindow().getWidth();
        float height = Minecraft.getInstance().getWindow().getHeight();
        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Set up shader and texture
        this.setupRender(opacity, tickTime);
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

    public void vignette(RenderGuiLayerEvent.Pre event)
    {
        if (!this.test(Minecraft.getInstance().player)) return;
        LivingEntity entity = this.entity();
        float effect = (float) this.getEffectFactor();
        float tickTime = entity.tickCount + event.getPartialTick().getGameTimeDeltaPartialTick(true);

        if (event.getName() == VanillaGuiLayers.CAMERA_OVERLAYS)
        {
            // Setup calculations
            float opacity = CSMath.blend(0f, 1f, effect, 0, 1);
            if (opacity == 0) return;

            render(opacity, tickTime, event);
        }
    }

    @Override
    public Side getSide()
    {   return Side.CLIENT;
    }
}
