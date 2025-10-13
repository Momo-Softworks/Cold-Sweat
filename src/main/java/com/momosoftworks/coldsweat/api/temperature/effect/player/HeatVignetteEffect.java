package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.mojang.math.Vector4f;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class HeatVignetteEffect extends AbstractVignetteEffect
{
    public HeatVignetteEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/overlay/haze.png");

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
    @Override
    protected void render(float opacity, float tickTime, RenderGameOverlayEvent.PreLayer event)
    {
        opacity *= ConfigSettings.HEATSTROKE_BORDER_OPACITY.get();
        super.render(opacity, tickTime, event);
    }
}
