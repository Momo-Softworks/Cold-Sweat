package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class FreezeVignetteEffect extends HeatVignetteEffect
{
    public FreezeVignetteEffect(LivingEntity entity, IntegerBounds range)
    {   super(entity, range);
    }

    static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");
    @Override
    protected void setupRender(float opacity, float tickTime)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, opacity);
        RenderSystem.setShaderTexture(0, TEXTURE);
    }
}
