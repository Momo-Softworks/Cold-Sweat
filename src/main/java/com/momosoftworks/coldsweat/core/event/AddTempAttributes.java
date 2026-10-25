package com.momosoftworks.coldsweat.core.event;

import net.minecraft.world.entity.EntityTypes;
import com.momosoftworks.coldsweat.api.event.core.init.EnableTemperatureEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ServerConfigsLoadedEvent;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.core.init.ModAttributes;
import com.momosoftworks.coldsweat.core.init.ModEntities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

import java.lang.reflect.Field;
import java.util.Map;

import static com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager.TEMPERATURE_ENABLED_ENTITIES;

@EventBusSubscriber
public class AddTempAttributes
{
    @EventBusSubscriber
    public static class ForPlayer
    {
        @SubscribeEvent
        public static void addPlayerAttributes(EntityAttributeModificationEvent event)
        {
            event.add(EntityTypes.PLAYER, ModAttributes.COLD_DAMPENING, 0.0);
            event.add(EntityTypes.PLAYER, ModAttributes.HEAT_DAMPENING, 0.0);
            event.add(EntityTypes.PLAYER, ModAttributes.COLD_RESISTANCE, 0.0);
            event.add(EntityTypes.PLAYER, ModAttributes.HEAT_RESISTANCE, 0.0);
            event.add(EntityTypes.PLAYER, ModAttributes.BURNING_POINT, 0.0);
            event.add(EntityTypes.PLAYER, ModAttributes.FREEZING_POINT, 0.0);
            event.add(EntityTypes.PLAYER, ModAttributes.BASE_BODY_TEMPERATURE, 0.0);
            event.add(EntityTypes.PLAYER, ModAttributes.WORLD_TEMPERATURE, 0.0);
            event.add(EntityTypes.PLAYER, ModAttributes.TEMP_RATE, 0.0);
        }
    }

    private static final Field FORGE_ATTRIBUTES;
    static
    {
        try
        {   FORGE_ATTRIBUTES = CommonHooks.class.getDeclaredField("FORGE_ATTRIBUTES");
            FORGE_ATTRIBUTES.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {   throw new RuntimeException(e);
        }
    }

    private static Map<EntityType<? extends LivingEntity>, AttributeSupplier> getForgeAttributes()
    {
        try
        {   return (Map<EntityType<? extends LivingEntity>, AttributeSupplier>) FORGE_ATTRIBUTES.get(null);
        }
        catch (IllegalAccessException e)
        {   throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onEntitiesCreated(ServerConfigsLoadedEvent event)
    {
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE)
        {
            EntityType<? extends LivingEntity> type;
            try
            {   type = (EntityType<? extends LivingEntity>) entityType;
            }
            catch (ClassCastException e)
            {   continue;
            }

            if (type == EntityTypes.PLAYER) continue;

            EnableTemperatureEvent enableEvent = new EnableTemperatureEvent(type);
            NeoForge.EVENT_BUS.post(enableEvent);
            if (!enableEvent.isEnabled() || enableEvent.isCanceled()) continue;

            TEMPERATURE_ENABLED_ENTITIES.add(type);

            AttributeSupplier attributes = CSMath.orElse(getForgeAttributes().get(type), DefaultAttributes.getSupplier(type));
            if (attributes == null) continue;
            AttributeSupplier.Builder builder = new AttributeSupplier.Builder(attributes);

            builder.add(ModAttributes.COLD_DAMPENING, 0.0);
            builder.add(ModAttributes.HEAT_DAMPENING, 0.0);
            builder.add(ModAttributes.COLD_RESISTANCE, 0.0);
            builder.add(ModAttributes.HEAT_RESISTANCE, 0.0);
            builder.add(ModAttributes.BURNING_POINT, 0.0);
            builder.add(ModAttributes.FREEZING_POINT, 0.0);
            builder.add(ModAttributes.BASE_BODY_TEMPERATURE, 0.0);
            builder.add(ModAttributes.WORLD_TEMPERATURE, 0.0);
            builder.add(ModAttributes.TEMP_RATE, 0.0);

            getForgeAttributes().put(type, builder.build());
        }
    }

    /**
     * Enable temperature handling for chameleons
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEnableTemperatureEvent(EnableTemperatureEvent event)
    {
        if (event.getEntityType() == ModEntities.CHAMELEON
       .value() || (ConfigSettings.ENABLE_ENTITY_CLIMATES.get() && EntityTempManager.hasClimateData(event.getEntityType())))
        {   event.setEnabled(true);
        }
    }
}
