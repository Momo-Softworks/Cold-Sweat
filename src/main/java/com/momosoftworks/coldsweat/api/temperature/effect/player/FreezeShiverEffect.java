package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreezeShiverEffect extends TempEffect
{
    public FreezeShiverEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void shiverCamera(EntityViewRenderEvent.CameraSetup event)
    {
        if (!this.test(Minecraft.getInstance().player)) return;
        LivingEntity entity = this.entity();

        if (!Minecraft.getInstance().isPaused() && ConfigSettings.DISTORTION_EFFECTS.get())
        {
            double effect = this.getEffectFactor();
            double partialTick = event.getRenderPartialTicks();
            double tickTime = entity.tickCount + partialTick;
            float shiverIntensity = (float) CSMath.blend(0, Math.sin(tickTime / 10) * 0.3f, effect, 0, 1);
            shiverIntensity *= ConfigSettings.SHIVER_INTENSITY.get();
            // Rotate camera
            float shiverRotation = (float) (Math.sin(tickTime * 2.5) * shiverIntensity);
            entity.yRot = entity.yRot + shiverRotation;
        }
    }

    @Override
    public Side getSide()
    {   return Side.CLIENT;
    }
}
