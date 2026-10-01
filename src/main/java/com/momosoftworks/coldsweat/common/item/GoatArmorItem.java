package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.core.init.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Custom armor model is registered in {@link com.momosoftworks.coldsweat.client.event.RegisterClientItemExtensions}
 */
public class GoatArmorItem extends Item
{
    public GoatArmorItem(Properties properties)
    {   super(properties);
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer)
    {   return stack.is(ModItems.GOAT_FUR_BOOTS);
    }
}
