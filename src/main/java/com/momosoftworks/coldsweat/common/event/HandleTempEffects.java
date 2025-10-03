package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.TempEffectsData;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class HandleTempEffects
{
    @SubscribeEvent
    public static void addTempEffects(LivingEvent.LivingUpdateEvent event)
    {
        LivingEntity living = event.getEntityLiving();
        if (living.tickCount % 20 == 0)
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
                    cap.addTempEffect(effectType.create(effectType, living, holder.range()), living.level.isClientSide);
                    // Mark effect as added
                    addedEffects.add(effectType);
                });
                // Remove effects that are not applicable to the entity
                cap.getTempEffects().keySet().stream().filter(type -> !addedEffects.contains(type)).forEach(cap::removeTempEffect);
            });
        }
    }

    @SubscribeEvent
    public static void clearTempEffects(EntityLeaveWorldEvent event)
    {
        if (event.getEntity() instanceof LivingEntity)
        {   EntityTempManager.getTemperatureCap(event.getEntity()).ifPresent(ITemperatureCap::clearTempEffects);
        }
    }
}
