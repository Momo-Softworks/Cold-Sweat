package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemTempData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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
    {       effectsPerTrait.clear();

        Map<ItemTempData, Double> effectsPerItemTemp = new HashMap<>();

        // Get temperature of equipped items
        for (EquipmentSlotType slot : EquipmentSlotType.values())
        {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty())
            {
                Item item = stack.getItem();
                ConfigSettings.ITEM_TEMPERATURES.get().get(item).forEach(
                itemData ->
                {   checkAndAddItemTemp(entity, stack, -1, slot, itemData, effectsPerItemTemp);
                });
            }
        }

        // Get temperature of main inventory items
        if (entity instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) entity;
            for (Slot slot : player.inventoryMenu.slots)
            {
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty())
                {
                    Item item = stack.getItem();
                    ConfigSettings.ITEM_TEMPERATURES.get().get(item).forEach(
                    itemData ->
                    {   checkAndAddItemTemp(entity, stack, slot.getSlotIndex(), null, itemData, effectsPerItemTemp);
                    });
                }
            }
        }

        for (Map.Entry<ItemTempData, Double> entry : effectsPerItemTemp.entrySet())
        {
            Temperature.Trait dataTrait = entry.getKey().trait();
            double temp = entry.getValue();

            effectsPerTrait.put(dataTrait, effectsPerTrait.getOrDefault(dataTrait, 0.0) + temp);
        }
    }

    private static void checkAndAddItemTemp(LivingEntity entity, ItemStack stack, int slot, EquipmentSlotType equipmentSlot,
                                            ItemTempData itemData, Map<ItemTempData, Double> effectsPerItemTemp)
    {
        if (itemData.test(entity, stack, slot, equipmentSlot))
        {
            double temp = itemData.getTemperature(entity, stack) * stack.getCount();
            double currentEffect = effectsPerItemTemp.getOrDefault(itemData, 0.0);
            double newEffect = currentEffect + temp;
            // Clamp against maxEffect bounds
            double maxEffect = itemData.maxEffect(stack, entity);
        newEffect = temp > 0 ? Math.min(maxEffect, newEffect) : Math.max(-maxEffect, newEffect);
            // Clamp against minTemp/maxTemp bounds
            double minTemp = itemData.minTemp(stack, entity);
        double maxTemp = itemData.maxTemp(stack, entity);
        newEffect = CSMath.clamp(newEffect, minTemp, maxTemp);

            effectsPerItemTemp.put(itemData, newEffect);
        }
    }
}
