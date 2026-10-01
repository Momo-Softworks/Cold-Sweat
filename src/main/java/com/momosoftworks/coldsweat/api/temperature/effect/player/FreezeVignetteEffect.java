package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.joml.Vector4f;

public class FreezeVignetteEffect extends AbstractVignetteEffect
{
    public FreezeVignetteEffect(TempEffectType<?> type, IntegerBounds bounds)
    {   super(type, bounds);
    }

    static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/misc/powder_snow_outline.png");

    @Override
    protected Identifier getTexture()
    {   return TEXTURE;
    }

    @Override
    protected Vector4f getColor(float tickTime)
    {   return new Vector4f(1f, 1f, 1f, 1f);
    }

    @SubscribeEvent
    @Override
    public void vignette(RenderGuiLayerEvent.Pre event)
    {   super.vignette(event);
    }

    @Override
    protected void render(float opacity, float tickTime, RenderGuiLayerEvent.Pre event)
    {
        opacity *= ConfigSettings.FREEZING_OVERLAY_OPACITY.get();
        super.render(opacity, tickTime, event);
    }
}
