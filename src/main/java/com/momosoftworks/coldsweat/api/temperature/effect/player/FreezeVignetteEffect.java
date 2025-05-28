package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class FreezeVignetteEffect extends HeatVignetteEffect
{
    public FreezeVignetteEffect(LivingEntity entity, IntegerBounds range)
    {   super(entity, range);
    }

    static final ResourceLocation TEXTURE = new ResourceLocation("textures/misc/powder_snow_outline.png");
    @Override
    protected void setupRender(float opacity, float tickTime)
    {
        RenderSystem.color4f(1f, 1f, 1f, opacity);
        Minecraft.getInstance().textureManager.bind(TEXTURE);
    }
}
