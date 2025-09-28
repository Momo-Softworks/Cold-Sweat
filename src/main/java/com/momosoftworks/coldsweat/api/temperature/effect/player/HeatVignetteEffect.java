package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.joml.Vector4f;

public class HeatVignetteEffect extends AbstractVignetteEffect
{
    public HeatVignetteEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/overlay/haze.png");

    @Override
    protected ResourceLocation getTexture()
    {   return TEXTURE;
    }

    @Override
    protected Vector4f getColor(float tickTime)
    {
        float effectFactor = (float) CSMath.blendLog(0, 1, this.getEffectFactor(), 0, 1, 4);
        float vignetteBrightness = (float) (Math.sin((tickTime + 3) / Math.PI) / 2 + 0.5f) * effectFactor;
        return new Vector4f(0.231f, 0f, 0f, vignetteBrightness);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    @Override
    public void vignette(RenderGuiLayerEvent.Pre event)
    {   super.vignette(event);
    }

    @Override
    protected void render(float opacity, float tickTime, RenderGuiLayerEvent.Pre event)
    {
        opacity *= ConfigSettings.HEATSTROKE_BORDER_OPACITY.get();
        super.render(opacity, tickTime, event);
    }
}
