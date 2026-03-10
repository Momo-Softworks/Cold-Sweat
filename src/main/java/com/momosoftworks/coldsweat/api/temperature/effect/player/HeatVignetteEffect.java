package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.ColdSweat;
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

public class HeatVignetteEffect extends AbstractVignetteEffect
{
    public HeatVignetteEffect(TempEffectType<?> type, IntegerBounds bounds)
    {   super(type, bounds);
    }

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/overlay/haze.png");

    @Override
    protected ResourceLocation getTexture()
    {   return TEXTURE;
    }

    @Override
    protected Vector4f getColor(float tickTime)
    {
        float vignetteBrightness = (float) (Math.sin((tickTime + 3) / Math.PI) / 2 + 0.5f);
        return new Vector4f(0.231f, 0f, 0f, vignetteBrightness);
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
        opacity *= ConfigSettings.HEATSTROKE_BORDER_OPACITY.get();
        super.render(opacity, tickTime, event);
    }
}
