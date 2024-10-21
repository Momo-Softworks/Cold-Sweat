package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.api.event.core.init.EnableTemperatureEvent;
import com.momosoftworks.coldsweat.util.registries.ModAttributes;
import com.momosoftworks.coldsweat.util.registries.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;

import static com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager.TEMPERATURE_ENABLED_ENTITIES;

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

            event.add(type, ModAttributes.COLD_DAMPENING, Double.NaN);
            event.add(type, ModAttributes.HEAT_DAMPENING, Double.NaN);
            event.add(type, ModAttributes.COLD_RESISTANCE, Double.NaN);
            event.add(type, ModAttributes.HEAT_RESISTANCE, Double.NaN);
            event.add(type, ModAttributes.BURNING_POINT, Double.NaN);
            event.add(type, ModAttributes.FREEZING_POINT, Double.NaN);
            event.add(type, ModAttributes.BASE_BODY_TEMPERATURE, Double.NaN);
            event.add(type, ModAttributes.WORLD_TEMPERATURE, Double.NaN);
        }
    }

    /**
     * Enable temperature handling for chameleons
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEnableTemperatureEvent(EnableTemperatureEvent event)
    {
        if (event.getEntityType() == ModEntities.CHAMELEON)
        {   event.setEnabled(true);
        }
    }
}
