package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.mojang.blaze3d.shaders.Uniform;
import com.momosoftworks.coldsweat.api.event.vanilla.RenderLevelEvent;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.client.renderer.PostProcessShaderManager;
import com.momosoftworks.coldsweat.common.event.HandleTempEffects;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.momosoftworks.coldsweat.common.event.HandleTempEffects.Client.HOT_IMMUNITY;

public class HeatBlurEffect extends TempEffect
{
    public HeatBlurEffect(LivingEntity entity, IntegerBounds bounds)
    {   super(entity, bounds);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderBlur(RenderLevelEvent.Post event)
    {
        Player player = Minecraft.getInstance().player;
        if (!this.test(player)) return;
        if (HandleTempEffects.isPlayerImmune(player)) return;

        double blurMultiplier = ConfigSettings.HEATSTROKE_BLUR_AMOUNT.get();
        if (blurMultiplier == 0) return;
        PostProcessShaderManager shaderManager = PostProcessShaderManager.getInstance();

        if (ConfigSettings.DISTORTION_EFFECTS.get() && Overlays.BLEND_BODY_TEMP >= 50 && HOT_IMMUNITY < 1)
        {
            float blur = (float) CSMath.blend(0, 12, Overlays.BLEND_BODY_TEMP, this.bounds().min(), this.bounds().max());
            blur = (float) CSMath.blend(blur, 0, HOT_IMMUNITY, 0, 1);
            blur *= blurMultiplier;
            if (!shaderManager.hasEffect("heat_blur"))
            {   shaderManager.loadEffect("heat_blur", PostProcessShaderManager.BLOBS);
            }
            Uniform blurRadius = shaderManager.getPostPasses("heat_blur").get(0).getEffect().getUniform("Radius");
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
    public boolean isClient()
    {   return true;
    }
}
