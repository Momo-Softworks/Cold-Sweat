package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.event.HandleTempEffects;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.momosoftworks.coldsweat.common.event.HandleTempEffects.Client.HOT_IMMUNITY;

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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void swayCamera(ViewportEvent.ComputeCameraAngles event)
    {
        Player player = Minecraft.getInstance().player;

        if (!this.test(player)) return;
        if (HandleTempEffects.isPlayerImmune(player)) return;

        float frameTime = Minecraft.getInstance().getDeltaFrameTime();

        if (!Minecraft.getInstance().isPaused())
        {
            if (ConfigSettings.DISTORTION_EFFECTS.get())
            {
                // Camera "shivers" when temp is < -50
                if (Overlays.BLEND_BODY_TEMP >= 50 && HOT_IMMUNITY < 1)
                {
                    float factor = (float) CSMath.blend(0, 20, Overlays.BLEND_BODY_TEMP, this.bounds().min(), this.bounds().max());
                    factor = (float) CSMath.blend(factor, 0, HOT_IMMUNITY, 0, 1);
                    factor *= ConfigSettings.HEATSTROKE_SWAY_AMOUNT.get();

                    // Set random sway speed every once in a while
                    if (TIME_SINCE_NEW_SWAY > 100 || X_SWAY_SPEED == 0 || Y_SWAY_SPEED == 0)
                    {
                        TIME_SINCE_NEW_SWAY = 0;
                        X_SWAY_SPEED = (float) ((Math.random() * 0.002f + 0.0025f) * ConfigSettings.HEATSTROKE_SWAY_SPEED.get());
                        Y_SWAY_SPEED = (float) ((Math.random() * 0.002f + 0.0025f) * ConfigSettings.HEATSTROKE_SWAY_SPEED.get());
                    }
                    TIME_SINCE_NEW_SWAY += frameTime;

                    // Blend to the new sway speed
                    X_SWAY_PHASE += 2 * Math.PI * frameTime * X_SWAY_SPEED;
                    Y_SWAY_PHASE += 2 * Math.PI * frameTime * Y_SWAY_SPEED;

                    // Apply the sway speed to a sin function
                    float xOffs = (float) (Math.sin(X_SWAY_PHASE) * factor);
                    float yOffs = (float) ((Math.sin(Y_SWAY_PHASE) + Math.cos(Y_SWAY_PHASE / 4) * 2) * factor * 3);

                    // Apply the sway
                    player.setXRot(player.getXRot() + xOffs - PREV_X_SWAY);
                    player.setYRot(player.getYRot() + yOffs - PREV_Y_SWAY);

                    // Save the previous sway
                    PREV_X_SWAY = xOffs;
                    PREV_Y_SWAY = yOffs;
                }
            }
        }
    }

    @Override
    public boolean isClient()
    {   return true;
    }
}
