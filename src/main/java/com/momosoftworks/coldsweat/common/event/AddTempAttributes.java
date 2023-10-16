package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.event.common.EnableTemperatureEvent;
import com.momosoftworks.coldsweat.util.registries.ModAttributes;
import com.momosoftworks.coldsweat.util.registries.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;

import static com.momosoftworks.coldsweat.common.capability.EntityTempManager.TEMPERATURE_ENABLED_ENTITIES;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AddTempAttributes
{
    @SubscribeEvent
    public static void onEntitiesCreated(EntityAttributeModificationEvent event)
    {
        for (EntityType<? extends LivingEntity> type : event.getTypes())
        {
            if (type != EntityType.PLAYER)
            {   EnableTemperatureEvent enableEvent = new EnableTemperatureEvent(type);
                ModLoader.get().postEvent(enableEvent);
                if (!enableEvent.isEnabled() || enableEvent.isCanceled()) continue;
            }
            TEMPERATURE_ENABLED_ENTITIES.add(type);
            event.add(type, ModAttributes.COLD_DAMPENING, 0.0);
            event.add(type, ModAttributes.HEAT_DAMPENING, 0.0);
            event.add(type, ModAttributes.BURNING_POINT_OFFSET, 0.0);
            event.add(type, ModAttributes.FREEZING_POINT_OFFSET, 0.0);
            event.add(type, ModAttributes.COLD_RESISTANCE_OFFSET, 0.0);
            event.add(type, ModAttributes.HEAT_RESISTANCE_OFFSET, 0.0);
            event.add(type, ModAttributes.CORE_BODY_TEMPERATURE_OFFSET, 0.0);
            event.add(type, ModAttributes.BASE_BODY_TEMPERATURE_OFFSET, 0.0);
            event.add(type, ModAttributes.WORLD_TEMPERATURE_OFFSET, 0.0);
            // Read-only
            event.add(type, ModAttributes.BURNING_POINT, 0.0);
            event.add(type, ModAttributes.FREEZING_POINT, 0.0);
            event.add(type, ModAttributes.CORE_BODY_TEMPERATURE, 0.0);
            event.add(type, ModAttributes.BASE_BODY_TEMPERATURE, 0.0);
            event.add(type, ModAttributes.WORLD_TEMPERATURE, 0.0);
        }
    }

    /**
     * Enable temperature handling for chameleons
     */
    @SubscribeEvent
    public static void onEnableTemperatureEvent(EnableTemperatureEvent event)
    {
        if (event.getEntityType() == ModEntities.CHAMELEON)
        {   event.setEnabled(true);
        }
    }
}
