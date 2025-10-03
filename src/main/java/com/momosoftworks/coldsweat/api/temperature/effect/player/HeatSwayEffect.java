package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HeatSwayEffect extends TempEffect
{
    public HeatSwayEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    static float PREV_X_SWAY = 0;
    static float PREV_Y_SWAY = 0;
    static float X_SWAY_SPEED = 0;
    static float Y_SWAY_SPEED = 0;
    static float X_SWAY_PHASE = 0;
    static float Y_SWAY_PHASE = 0;
    static float TIME_SINCE_NEW_SWAY = 0;
    static float SWAY_FACTOR = 0;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void swayCamera(EntityViewRenderEvent.CameraSetup event)
    {
        PlayerEntity player = Minecraft.getInstance().player;

        if (!this.test(player))
        {   SWAY_FACTOR = 0;
            PREV_X_SWAY = 0;
            PREV_Y_SWAY = 0;
            return;
        }

        float frameTime = Minecraft.getInstance().getDeltaFrameTime();
        double effect = this.getEffectFactor();

        if (!Minecraft.getInstance().isPaused() && ConfigSettings.DISTORTION_EFFECTS.get())
        {
            float targetFactor = (float) CSMath.blend(0, 20, effect, 0, 1);
            targetFactor *= ConfigSettings.HEATSTROKE_SWAY_AMOUNT.get();

            float interpolationSpeed = 0.05f * frameTime;
            SWAY_FACTOR = SWAY_FACTOR + (targetFactor - SWAY_FACTOR) * interpolationSpeed;

            // Set random sway speed every once in a while
            if (TIME_SINCE_NEW_SWAY > 100 || X_SWAY_SPEED == 0 || Y_SWAY_SPEED == 0)
            {
                TIME_SINCE_NEW_SWAY = 0;
                X_SWAY_SPEED = (float) ((Math.random() * 0.001f + 0.0025f) * ConfigSettings.HEATSTROKE_SWAY_SPEED.get());
                Y_SWAY_SPEED = (float) ((Math.random() * 0.001f + 0.0025f) * ConfigSettings.HEATSTROKE_SWAY_SPEED.get());
            }
            TIME_SINCE_NEW_SWAY += frameTime;

            // Blend to the new sway speed
            X_SWAY_PHASE += 2 * Math.PI * frameTime * X_SWAY_SPEED;
            Y_SWAY_PHASE += 2 * Math.PI * frameTime * Y_SWAY_SPEED;

            // Apply the sway speed to a sin function using the smoothed factor
            float xOffs = (float) (Math.sin(X_SWAY_PHASE) * SWAY_FACTOR);
            float yOffs = (float) ((Math.sin(Y_SWAY_PHASE) + Math.cos(Y_SWAY_PHASE / 4) * 2) * SWAY_FACTOR * 3);

            // Apply the sway
            player.xRot = player.xRot + xOffs - PREV_X_SWAY;
            player.yRot = player.yRot + yOffs - PREV_Y_SWAY;

            // Save the previous sway
            PREV_X_SWAY = xOffs;
            PREV_Y_SWAY = yOffs;
        }
    }

    @Override
    public Side getSide()
    {   return Side.CLIENT;
    }
}
