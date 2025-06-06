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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

import static com.momosoftworks.coldsweat.common.event.HandleTempEffects.Client.COLD_IMMUNITY;

public class FreezeShiverEffect extends TempEffect
{
    public FreezeShiverEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void shiverCamera(ViewportEvent.ComputeCameraAngles event)
    {
        Player player = Minecraft.getInstance().player;
        if (!this.test(player)) return;
        if (HandleTempEffects.isPlayerImmune(player)) return;

        if (!Minecraft.getInstance().isPaused())
        {
            if (ConfigSettings.DISTORTION_EFFECTS.get())
            {
                // Camera "shivers" when temp is < -50
                if (Overlays.BLEND_BODY_TEMP <= -50 && COLD_IMMUNITY < 1)
                {
                    double tickTime = player.tickCount + event.getPartialTick();
                    float shiverIntensity = (float) CSMath.blend(0, (Math.sin(tickTime / 10) + 1) * 0.03f + 0.01f, Overlays.BLEND_BODY_TEMP, this.bounds().min(), this.bounds().max());
                    shiverIntensity *= ConfigSettings.SHIVER_INTENSITY.get();
                    // Multiply the effect for lower framerates
                    shiverIntensity *= Minecraft.getInstance().getTimer().getRealtimeDeltaTicks() * 10;
                    // Factor in cold immunity
                    shiverIntensity = (float) CSMath.blend(shiverIntensity, 0, COLD_IMMUNITY, 0, 1);
                    // Rotate camera
                    float shiverRotation = (float) (Math.sin(tickTime * 2.5) * shiverIntensity);
                    player.setYRot(player.getYRot() + shiverRotation);
                }
            }
        }
    }

    @Override
    public boolean isClient()
    {   return true;
    }
}
