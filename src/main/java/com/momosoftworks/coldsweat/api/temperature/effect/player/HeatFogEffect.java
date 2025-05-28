package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.event.client.RenderFogEvent;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.event.HandleTempEffects;
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

import static com.momosoftworks.coldsweat.common.event.HandleTempEffects.Client.HOT_IMMUNITY;

public class HeatFogEffect extends TempEffect
{
    public HeatFogEffect(LivingEntity entity, IntegerBounds range)
    {   super(entity, range);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void renderHeatFog(EntityViewRenderEvent event)
    {
        PlayerEntity player = Minecraft.getInstance().player;

        if (!this.test(player)) return;
        if (!(event instanceof RenderFogEvent || event instanceof EntityViewRenderEvent.FogColors)) return;

        if (HandleTempEffects.isPlayerImmune(player)) return;

        double fogDistance = ConfigSettings.HEATSTROKE_FOG_DISTANCE.get();
        if (fogDistance >= 64) return;
        if (fogDistance < Double.POSITIVE_INFINITY && Overlays.BLEND_BODY_TEMP >= 50 && HOT_IMMUNITY < 1)
        {
            float tempWithResistance = (float) CSMath.blend(Overlays.BLEND_BODY_TEMP, 50, HOT_IMMUNITY, 0, 1);
            if (event instanceof RenderFogEvent)
            {
                RenderFogEvent fog = (RenderFogEvent) event;
                if (fogDistance > (fog.getFarPlaneDistance())) return;
                fog.setFarPlaneDistance(CSMath.blend(fog.getFarPlaneDistance(), (float) fogDistance, tempWithResistance, 50f, 90f));
                fog.setNearPlaneDistance(CSMath.blend(fog.getNearPlaneDistance(), (float) (fogDistance * 0.3), tempWithResistance, 50f, 90f));
                fog.setCanceled(true);
            }
            else
            {   EntityViewRenderEvent.FogColors fogColor = (EntityViewRenderEvent.FogColors) event;
                fogColor.setRed(CSMath.blend(fogColor.getRed(), 0.01f, tempWithResistance, 50, 90));
                fogColor.setGreen(CSMath.blend(fogColor.getGreen(), 0.01f, tempWithResistance, 50, 90));
                fogColor.setBlue(CSMath.blend(fogColor.getBlue(), 0.05f, tempWithResistance, 50, 90));
            }
        }
    }

    @Override
    protected boolean isClient()
    {   return true;
    }
}
