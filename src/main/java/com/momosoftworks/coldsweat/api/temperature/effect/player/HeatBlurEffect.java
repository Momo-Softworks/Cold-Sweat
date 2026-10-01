package com.momosoftworks.coldsweat.api.temperature.effect.player;

import net.minecraft.world.level.Level;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.api.event.vanilla.RenderLevelEvent;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.client.renderer.PostProcessShaderManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;


public class HeatBlurEffect extends TempEffect
{
    public HeatBlurEffect(TempEffectType<?> type, IntegerBounds bounds)
    {   super(type, bounds);
    }

    @SubscribeEvent
    public void onRenderBlur(RenderLevelEvent.Post event)
    {
        PostProcessShaderManager shaderManager = PostProcessShaderManager.getInstance();
        LivingEntity player = ClientOnlyHelper.getClientPlayer();
        if (!this.test(player))
        {   shaderManager.clearEffect();
            return;
        }

        double effect = this.getEffectFactor(player);
        float blurMultiplier = ConfigSettings.HEATSTROKE_BLUR_AMOUNT.get().floatValue();
        if (blurMultiplier == 0 || !ConfigSettings.DISTORTION_EFFECTS.get())
        {   shaderManager.clearEffect();
            return;
        }

        float blur = (float) CSMath.blend(0, 12, effect, 0, 1) * blurMultiplier;
        if (blur >= 1)
        {   shaderManager.setEffect(PostProcessShaderManager.HEAT_BLUR);
            shaderManager.setUniforms(PostProcessShaderManager.HEAT_BLUR, 0, "BlobConfig", builder -> builder.putFloat(blur));
        }
        else shaderManager.clearEffect();
    }

    @Override
    public Side getSide()
    {   return Side.CLIENT;
    }
}
