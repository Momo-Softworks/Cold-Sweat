package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import earth.terrarium.adastra.common.tags.ModItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Function;
import java.util.stream.StreamSupport;

public class AdAstraOxygenTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        if (!entity.level().isClientSide
        && !OxygenApi.API.hasOxygen(entity.level(), entity.blockPosition())
        && SpaceSuitItem.getOxygenAmount(entity) > 0)
        {   return temp -> CSMath.blend(temp, Temperature.getNeutralWorldTemp(entity), 0.85, 0, 1);
        }
        return temp -> temp;
    }

    @SubscribeEvent
    public static void onEquipmentChanged(LivingEquipmentChangeEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (EntityTempManager.isTemperatureEnabled(entity))
        {
            if (StreamSupport.stream(entity.getArmorSlots().spliterator(), true).allMatch(item -> item.is(ModItemTags.SPACE_SUITS)))
            {
                AdAstraOxygenTempModifier modifier = new AdAstraOxygenTempModifier();
                Temperature.addModifier(entity, modifier, Temperature.Trait.WORLD, Placement.Duplicates.BY_CLASS);
            }
            else if (Temperature.hasModifier(entity, Temperature.Trait.WORLD, AdAstraOxygenTempModifier.class))
            {   Temperature.removeModifiers(entity, Temperature.Trait.WORLD, AdAstraOxygenTempModifier.class);
            }
        }
    }
}
