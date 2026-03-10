package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import org.joml.Vector4f;

public class FreezeVignetteEffect extends AbstractVignetteEffect
{
    public FreezeVignetteEffect(TempEffectType<?> type, IntegerBounds bounds)
    {   super(type, bounds);
    }

    static final ResourceLocation TEXTURE = new ResourceLocation("textures/misc/powder_snow_outline.png");

    @Override
    protected ResourceLocation getTexture()
    {   return TEXTURE;
    }

    @Override
    protected Vector4f getColor(float tickTime)
    {   return new Vector4f(1f, 1f, 1f, 1f);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void render(float opacity, float tickTime, RenderGuiOverlayEvent.Pre event)
    {
        opacity *= ConfigSettings.FREEZING_OVERLAY_OPACITY.get();
        super.render(opacity, tickTime, event);
    }
}
