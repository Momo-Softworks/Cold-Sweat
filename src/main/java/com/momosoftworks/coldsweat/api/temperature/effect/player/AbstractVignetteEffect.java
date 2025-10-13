package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector4f;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        RenderSystem.setShaderColor(color.x(), color.y(), color.z(), opacity * color.w());
        RenderSystem.setShaderTexture(0, this.getTexture());
    }

    @OnlyIn(Dist.CLIENT)
    protected void render(float opacity, float tickTime, RenderGuiOverlayEvent.Pre event)
    {
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void vignette(RenderGuiOverlayEvent.Pre event)
    {
        if (!this.test(Minecraft.getInstance().player)) return;
        LivingEntity entity = this.entity();
        float effect = (float) this.getEffectFactor();
        float tickTime = entity.tickCount + event.getPartialTick();

        if (event.getOverlay() == VanillaGuiOverlay.VIGNETTE.type())
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
