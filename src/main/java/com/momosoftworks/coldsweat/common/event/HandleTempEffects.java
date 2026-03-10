package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModEffects;
import com.momosoftworks.coldsweat.data.codec.configuration.TempEffectsData;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class HandleTempEffects
{
    @SubscribeEvent
    public static void addTempEffects(EntityTickEvent.Pre event)
    {
        if (event.getEntity() instanceof LivingEntity living && living.tickCount % 20 == 0)
        {
            EntityTempManager.getTemperatureCap(living).ifPresent(cap ->
            {
                TempEffectsData effectsData = ConfigHelper.getFirstOrNull(ConfigSettings.ENTITY_TEMP_EFFECTS, living.getType(), data -> data.test(living));
                if (effectsData == null) return;

                List<TempEffectType<?>> addedEffects = new ArrayList<>();
                // Add temp effects
                effectsData.effects().forEach(holder ->
                {
                    TempEffectType effectType = holder.effect();
                    // Add effect
                    cap.addTempEffect(effectType.create(effectType, holder.range()), living.level().isClientSide);
                    // Mark effect as added
                    addedEffects.add(effectType);
                });
                // Remove effects that are not applicable to the entity
                cap.getTempEffects().keySet().stream().filter(type -> !addedEffects.contains(type)).forEach(cap::removeTempEffect);
            });
        }
    }

    @SubscribeEvent
    public static void clearTempEffects(EntityLeaveLevelEvent event)
    {
        if (event.getEntity() instanceof LivingEntity living)
        {   EntityTempManager.getTemperatureCap(living).ifPresent(ITemperatureCap::clearTempEffects);
        }
    }
}
