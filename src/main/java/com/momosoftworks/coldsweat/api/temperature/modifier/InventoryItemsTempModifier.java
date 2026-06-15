package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.function.Function;

/**
 * Items carried/worn by a player apply a temperature offset (defined in config).<br>
 * 1.7 substitute for 1.16's item-temperature handling.
 */
public class InventoryItemsTempModifier extends TempModifier
{
    public InventoryItemsTempModifier() {}

    @Override
    protected Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        if (!(entity instanceof EntityPlayer)) return temp -> temp;
        Map<net.minecraft.item.Item, Double> itemTemps = ConfigSettings.ITEM_TEMPERATURES.get();
        if (itemTemps.isEmpty()) return temp -> temp;

        EntityPlayer player = (EntityPlayer) entity;
        double total = 0;

        for (ItemStack stack : player.inventory.mainInventory)
        {   if (stack != null && itemTemps.containsKey(stack.getItem()))
            {   total += itemTemps.get(stack.getItem());
            }
        }
        for (ItemStack stack : player.inventory.armorInventory)
        {   if (stack != null && itemTemps.containsKey(stack.getItem()))
            {   total += itemTemps.get(stack.getItem());
            }
        }

        final double finalTemp = total;
        return temp -> temp + finalTemp;
    }

    public String getID()
    {   return "cold_sweat:inventory_items";
    }
}
