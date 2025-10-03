package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.event.vanilla.RenderLevelEvent;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.client.renderer.PostProcessShaderManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class HeatBlurEffect extends TempEffect
{
    public HeatBlurEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderBlur(RenderLevelEvent.Post event)
    {
        if (!this.test(Minecraft.getInstance().player)) return;

        double effect = this.getEffectFactor();
        float blurMultiplier = ConfigSettings.HEATSTROKE_BLUR_AMOUNT.get().floatValue();
        if (blurMultiplier == 0) return;
        PostProcessShaderManager shaderManager = PostProcessShaderManager.getInstance();

        if (ConfigSettings.DISTORTION_EFFECTS.get())
        {
            float blur = (float) CSMath.blend(0, 12, effect, 0, 1) * blurMultiplier;
            if (!shaderManager.hasEffect("heat_blur"))
            {   shaderManager.loadEffect("heat_blur", PostProcessShaderManager.BLOBS);
            }
            ShaderUniform blurRadius = shaderManager.getPostPasses("heat_blur").get(0).getEffect().getUniform("Radius");
            if (blurRadius != null)
            {   blurRadius.set(blur);
            }
        }
        else if (shaderManager.hasEffect("heat_blur"))
        {   shaderManager.closeEffect("heat_blur");
        }

        shaderManager.process(event.getPartialTick());
    }

    @Override
    public Side getSide()
    {   return Side.CLIENT;
    }
}
