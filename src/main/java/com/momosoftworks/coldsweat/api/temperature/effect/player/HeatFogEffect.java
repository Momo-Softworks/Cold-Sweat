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

public class HeatFogEffect extends TempEffect
{
    public HeatFogEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void renderHeatFog(ViewportEvent event)
    {
        Player player = Minecraft.getInstance().player;

        if (!this.test(player)) return;
        if (!(event instanceof ViewportEvent.RenderFog || event instanceof ViewportEvent.ComputeFogColor)) return;

        if (HandleTempEffects.isPlayerImmune(player)) return;

        double fogDistance = ConfigSettings.HEATSTROKE_FOG_DISTANCE.get();
        if (fogDistance >= 64) return;
        if (fogDistance < Double.POSITIVE_INFINITY && Overlays.BLEND_BODY_TEMP >= 50 && HOT_IMMUNITY < 1)
        {
            float tempWithResistance = (float) CSMath.blend(Overlays.BLEND_BODY_TEMP, 50, HOT_IMMUNITY, 0, 1);
            if (event instanceof ViewportEvent.RenderFog fog)
            {
                if (fogDistance > (fog.getFarPlaneDistance())) return;
                fog.setFarPlaneDistance(CSMath.blend(fog.getFarPlaneDistance(), (float) fogDistance, tempWithResistance, 50f, 90f));
                fog.setNearPlaneDistance(CSMath.blend(fog.getNearPlaneDistance(), (float) (fogDistance * 0.3), tempWithResistance, 50f, 90f));
                fog.setCanceled(true);
            }
            else
            {   ViewportEvent.ComputeFogColor fogColor = (ViewportEvent.ComputeFogColor) event;
                fogColor.setRed(CSMath.blend(fogColor.getRed(), 0.01f, tempWithResistance, 50, 90));
                fogColor.setGreen(CSMath.blend(fogColor.getGreen(), 0.01f, tempWithResistance, 50, 90));
                fogColor.setBlue(CSMath.blend(fogColor.getBlue(), 0.05f, tempWithResistance, 50, 90));
            }
        }
    }

    @Override
    public boolean isClient()
    {   return true;
    }
}
