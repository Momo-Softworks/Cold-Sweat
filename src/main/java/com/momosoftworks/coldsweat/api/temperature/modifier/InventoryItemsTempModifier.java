package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemTempData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class InventoryItemsTempModifier extends TempModifier
{
    private final Map<Temperature.Trait, Double> effectsPerTrait = new EnumMap<>(Temperature.Trait.class);

    public InventoryItemsTempModifier(double temp)
    {   this.getNBT().putDouble("Effect", temp);
    }

    public InventoryItemsTempModifier()
    {   this(0);
    }

    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        return temp -> temp + this.effectsPerTrait.getOrDefault(trait, 0.0);
    }

    @Override
    public void tick(LivingEntity entity)
    {
        effectsPerTrait.clear();

        Map<ItemTempData, Double> effectsPerItemTemp = new HashMap<>();

        // Get temperature of equipped items
        EntityTempManager.getItemTemperaturesOnEntity(entity).forEach((stack, data) ->
        {   addItemTemp(entity, stack, data, effectsPerItemTemp);
        });

        for (Map.Entry<ItemTempData, Double> entry : effectsPerItemTemp.entrySet())
        {
            Temperature.Trait dataTrait = entry.getKey().trait();
            double temp = entry.getValue();

            effectsPerTrait.put(dataTrait, effectsPerTrait.getOrDefault(dataTrait, 0.0) + temp);
        }
    }

    private static void addItemTemp(LivingEntity entity, ItemStack stack, ItemTempData itemData, Map<ItemTempData, Double> effectsPerItemTemp)
    {
        double temp = itemData.temperature() * stack.getCount();
        double currentEffect = effectsPerItemTemp.getOrDefault(itemData, 0.0);
        double newEffect = currentEffect + temp;
        // Clamp against maxEffect bounds
        newEffect = temp > 0 ? Math.min(itemData.maxEffect(), newEffect) : Math.max(-itemData.maxEffect(), newEffect);
        // Clamp against minTemp/maxTemp bounds
        newEffect = Math.max(itemData.minTemp(), Math.min(itemData.maxTemp(), newEffect));

        effectsPerItemTemp.put(itemData, newEffect);
    }
}
