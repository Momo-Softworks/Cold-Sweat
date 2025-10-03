package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.event.client.RenderFogEvent;
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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HeatFogEffect extends TempEffect
{
    public HeatFogEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
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
    public void renderHeatFog(EntityViewRenderEvent event)
    {
        if (!(event instanceof RenderFogEvent || event instanceof EntityViewRenderEvent.FogColors)) return;

        if (!this.test(Minecraft.getInstance().player))
        {   FOG_FAR_DISTANCE = -1;
            FOG_NEAR_DISTANCE = -1;
            FOG_FAR_DISTANCE_TARGET = -1;
            FOG_NEAR_DISTANCE_TARGET = -1;
            FOG_RED = -1;
            FOG_GREEN = -1;
            FOG_BLUE = -1;
            return;
        }

        double effect = this.getEffectFactor();

        float frameTime = Minecraft.getInstance().getDeltaFrameTime();
        float farLerpSpeed = 0.08f * frameTime;
        float nearLerpSpeed = 0.08f * frameTime;

        if (event instanceof RenderFogEvent)
        {
            RenderFogEvent fog = (RenderFogEvent) event;
            double fogDistance = ConfigSettings.HEATSTROKE_FOG_DISTANCE.get();
            if (fogDistance >= 64 || Double.isInfinite(fogDistance)) return;
            if (fogDistance >= fog.getFarPlaneDistance()) return;

            if (fog.getFarPlaneDistance() != 0)
                FOG_FAR_DISTANCE_TARGET = (float) CSMath.blendLog(fog.getFarPlaneDistance(), fogDistance, effect, 0, 1, 4);
            if (fog.getNearPlaneDistance() != 0)
                FOG_NEAR_DISTANCE_TARGET = (float) CSMath.blendLog(fog.getNearPlaneDistance(), fogDistance * 0.3, effect, 0, 1, 4);

            if (FOG_NEAR_DISTANCE <= 0) {
                FOG_FAR_DISTANCE = FOG_FAR_DISTANCE_TARGET;
                FOG_NEAR_DISTANCE = FOG_NEAR_DISTANCE_TARGET;
            }

            FOG_FAR_DISTANCE = FOG_FAR_DISTANCE + (FOG_FAR_DISTANCE_TARGET - FOG_FAR_DISTANCE) * farLerpSpeed;
            FOG_NEAR_DISTANCE = FOG_NEAR_DISTANCE + (FOG_NEAR_DISTANCE_TARGET - FOG_NEAR_DISTANCE) * nearLerpSpeed;

            fog.setFarPlaneDistance(FOG_FAR_DISTANCE);
            fog.setNearPlaneDistance(FOG_NEAR_DISTANCE);
            fog.setCanceled(true);
        }
        else
        {
            EntityViewRenderEvent.FogColors fogColor = (EntityViewRenderEvent.FogColors) event;

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
