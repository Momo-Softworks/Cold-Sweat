package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.joml.Vector4f;

public class FreezeVignetteEffect extends AbstractVignetteEffect
{
    public FreezeVignetteEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");

    @Override
    protected ResourceLocation getTexture()
    {   return TEXTURE;
    }

    @Override
    protected Vector4f getColor(float tickTime)
    {   return new Vector4f(1f, 1f, 1f, 1f);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    @Override
    public void vignette(RenderGuiLayerEvent.Pre event)
    {   super.vignette(event);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void render(float opacity, float tickTime, RenderGuiLayerEvent.Pre event)
    {
        opacity *= ConfigSettings.FREEZING_OVERLAY_OPACITY.get();
        super.render(opacity, tickTime, event);
    }
}
