package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.core.init.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Custom armor model is registered in {@link com.momosoftworks.coldsweat.client.event.RegisterClientItemExtensions}
 */
public class HoglinArmorItem extends Item
{
    public HoglinArmorItem(Properties properties)
    {   super(properties);
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer)
    {   return stack.is(ModItems.HOGLIN_HELMET);
    }
}
