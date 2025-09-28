package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreezeShiverEffect extends TempEffect
{
    public FreezeShiverEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void shiverCamera(ViewportEvent.ComputeCameraAngles event)
    {
        if (!this.test(Minecraft.getInstance().player)) return;
        LivingEntity entity = this.entity();

        if (!Minecraft.getInstance().isPaused() && ConfigSettings.DISTORTION_EFFECTS.get())
        {
            double effect = this.getEffectFactor();
            double tickTime = entity.tickCount + event.getPartialTick();
            float shiverIntensity = (float) CSMath.blend(0, (Math.sin(tickTime / 10) + 1) * 0.03f + 0.01f, effect, 0, 1);
            shiverIntensity *= ConfigSettings.SHIVER_INTENSITY.get();
            // Multiply the effect for lower framerates
            shiverIntensity *= Minecraft.getInstance().getDeltaFrameTime() * 10;
            // Rotate camera
            float shiverRotation = (float) (Math.sin(tickTime * 2.5) * shiverIntensity);
            entity.setYRot(entity.getYRot() + shiverRotation);
        }
    }

    @Override
    public Side getSide()
    {   return Side.CLIENT;
    }
}
