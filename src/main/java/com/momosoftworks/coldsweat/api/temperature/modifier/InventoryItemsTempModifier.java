package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemCarryTempData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@EventBusSubscriber
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
        if (trait != Temperature.Trait.ALL)
        {   return temp -> temp + this.effectsPerTrait.getOrDefault(trait, 0.0);
        }

        effectsPerTrait.clear();

        Map<ItemCarryTempData, Double> effectsPerCarriedTemp = new HashMap<>();

        // Get temperature of equipped items
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty())
            {
                Item item = stack.getItem();
                ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(item).forEach(
                carried ->
                {   checkAndAddCarriedTemp(entity, stack, null, slot, carried, effectsPerCarriedTemp);
                });
            }
        }

        // Get temperature of main inventory items
        if (entity instanceof Player player)
        {
            for (Slot slot : player.inventoryMenu.slots)
            {
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty())
                {
                    Item item = stack.getItem();
                    ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(item).forEach(
                    carried ->
                    {   checkAndAddCarriedTemp(entity, stack, slot.index, null, carried, effectsPerCarriedTemp);
                    });
                }
            }
        }

        for (Map.Entry<ItemCarryTempData, Double> entry : effectsPerCarriedTemp.entrySet())
        {
            Temperature.Trait dataTrait = entry.getKey().trait();
            double temp = entry.getValue();

            effectsPerTrait.put(dataTrait, effectsPerTrait.getOrDefault(dataTrait, 0.0) + temp);
        }
        return temp -> temp;
    }

    private static void checkAndAddCarriedTemp(LivingEntity entity, ItemStack stack, Integer slot, EquipmentSlot equipmentSlot,
                                               ItemCarryTempData carried, Map<ItemCarryTempData, Double> effectsPerCarriedTemp)
    {
        if (carried.test(entity, stack, slot, equipmentSlot))
        {
            double temp = carried.temperature() * stack.getCount();
            double currentEffect = effectsPerCarriedTemp.getOrDefault(carried, 0.0);
            double newEffect = Math.min(carried.maxEffect(), Math.abs(currentEffect + temp)) * CSMath.sign(currentEffect + temp);

            effectsPerCarriedTemp.put(carried, newEffect);
        }
    }
}
