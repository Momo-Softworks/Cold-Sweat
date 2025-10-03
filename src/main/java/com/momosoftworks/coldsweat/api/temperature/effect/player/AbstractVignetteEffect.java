package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
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
        RenderSystem.color4f(color.x(), color.y(), color.z(), opacity * color.w());
        Minecraft.getInstance().textureManager.bind(this.getTexture());
    }

    protected void render(float opacity, float tickTime, RenderGameOverlayEvent.Pre event)
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
        Tessellator tesselator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.vertex(0.0D, height / scale, -90.0D).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(width / scale, height / scale, -90.0D).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(width / scale, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void vignette(RenderGameOverlayEvent.Pre event)
    {
        if (!this.test(Minecraft.getInstance().player)) return;
        LivingEntity entity = this.entity();
        float effect = (float) this.getEffectFactor();
        float tickTime = entity.tickCount + event.getPartialTicks();

        if (event.getType() == RenderGameOverlayEvent.ElementType.VIGNETTE)
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
