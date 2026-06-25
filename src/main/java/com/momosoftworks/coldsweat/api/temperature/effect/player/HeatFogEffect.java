package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HeatFogEffect extends TempEffect
{
    public HeatFogEffect(TempEffectType<?> type, IntegerBounds bounds)
    {   super(type, bounds);
    }

    static float FOG_FAR_DISTANCE = -1;
    static float FOG_NEAR_DISTANCE = -1;
    static float FOG_FAR_DISTANCE_TARGET = -1;
    static float FOG_NEAR_DISTANCE_TARGET = -1;
    static float FOG_RED = -1;
    static float FOG_GREEN = -1;
    static float FOG_BLUE = -1;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW)
    public void renderHeatFog(ViewportEvent event)
    {
        if (!(event instanceof ViewportEvent.RenderFog || event instanceof ViewportEvent.ComputeFogColor)) return;

        LivingEntity player = Minecraft.getInstance().player;
        if (!this.test(player))
        {   FOG_FAR_DISTANCE = -1;
            FOG_NEAR_DISTANCE = -1;
            FOG_FAR_DISTANCE_TARGET = -1;
            FOG_NEAR_DISTANCE_TARGET = -1;
            FOG_RED = -1;
            FOG_GREEN = -1;
            FOG_BLUE = -1;
            return;
        }

        double effect = this.getEffectFactor(player);

        float frameTime = Minecraft.getInstance().getDeltaFrameTime();
        float farLerpSpeed = 0.08f * frameTime;
        float nearLerpSpeed = 0.08f * frameTime;

        if (event instanceof ViewportEvent.RenderFog fog)
        {
            double fogDistance = ConfigSettings.HEATSTROKE_FOG_DISTANCE.get();
            if (fogDistance >= 64 || Double.isInfinite(fogDistance)) return;

            float nearPlaneDistance = fog.getNearPlaneDistance();
            float farPlaneDistance = fog.getFarPlaneDistance();

            if (farPlaneDistance != 0)
                FOG_FAR_DISTANCE_TARGET = (float) CSMath.blendLog(farPlaneDistance, fogDistance, effect, 0, 1, 4);
            if (nearPlaneDistance != 0)
                FOG_NEAR_DISTANCE_TARGET = (float) CSMath.blendLog(nearPlaneDistance, fogDistance * 0.3, effect, 0, 1, 4);

            if (FOG_NEAR_DISTANCE <= 0) {
                FOG_FAR_DISTANCE = FOG_FAR_DISTANCE_TARGET;
                FOG_NEAR_DISTANCE = FOG_NEAR_DISTANCE_TARGET;
            }

            FOG_FAR_DISTANCE = FOG_FAR_DISTANCE + (FOG_FAR_DISTANCE_TARGET - FOG_FAR_DISTANCE) * farLerpSpeed;
            FOG_NEAR_DISTANCE = FOG_NEAR_DISTANCE + (FOG_NEAR_DISTANCE_TARGET - FOG_NEAR_DISTANCE) * nearLerpSpeed;

            fog.setFarPlaneDistance(Math.min(farPlaneDistance, FOG_FAR_DISTANCE));
            fog.setNearPlaneDistance(Math.min(nearPlaneDistance, FOG_NEAR_DISTANCE));
            fog.setCanceled(true);
        }
        else
        {
            ViewportEvent.ComputeFogColor fogColor = (ViewportEvent.ComputeFogColor) event;

            float targetRed = (float) CSMath.blend(fogColor.getRed(), 0.04, effect, 0, 1);
            float targetGreen = (float) CSMath.blend(fogColor.getGreen(), 0.01, effect, 0, 1);
            float targetBlue = (float) CSMath.blend(fogColor.getBlue(), 0.02, effect, 0, 1);

            if (FOG_RED < 0) {
                FOG_RED = targetRed;
                FOG_GREEN = targetGreen;
                FOG_BLUE = targetBlue;
            }

            FOG_RED = FOG_RED + (targetRed - FOG_RED) * farLerpSpeed;
            FOG_GREEN = FOG_GREEN + (targetGreen - FOG_GREEN) * farLerpSpeed;
            FOG_BLUE = FOG_BLUE + (targetBlue - FOG_BLUE) * farLerpSpeed;

            fogColor.setRed(FOG_RED);
            fogColor.setGreen(FOG_GREEN);
            fogColor.setBlue(FOG_BLUE);
        }
    }

    @Override
    public Side getSide()
    {   return Side.CLIENT;
    }
}
