package com.momosoftworks.coldsweat.api.temperature.effect.player;

import net.minecraft.world.level.Level;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.vertex.*;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.joml.Vector4f;

public abstract class AbstractVignetteEffect extends TempEffect
{
    public AbstractVignetteEffect(TempEffectType<?> type, IntegerBounds bounds)
    {   super(type, bounds);
    }

    protected abstract Identifier getTexture();
    protected abstract Vector4f getColor(float tickTime);

    protected void render(float opacity, float tickTime, RenderGuiLayerEvent.Pre event)
    {
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        Vector4f color = this.getColor(tickTime);
        int argb = ARGB.colorFromFloat(opacity * color.w, color.x, color.y, color.z);
        // Stretch the whole texture over the screen
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.getTexture(), 0, 0, 0f, 0f, width, height, width, height, argb);
    }

    public void vignette(RenderGuiLayerEvent.Pre event)
    {
        LivingEntity entity = ClientOnlyHelper.getClientPlayer();
        if (!this.test(entity)) return;
        float effect = (float) this.getEffectFactor(entity);
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
