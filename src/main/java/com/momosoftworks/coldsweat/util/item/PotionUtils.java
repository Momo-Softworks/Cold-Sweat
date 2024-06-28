package com.momosoftworks.coldsweat.util.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;

public class PotionUtils
{
    public static ItemStack setPotion(ItemStack stack, Holder<Potion> potion)
    {
        if (stack.has(DataComponents.POTION_CONTENTS))
        {   stack.set(DataComponents.POTION_CONTENTS, stack.get(DataComponents.POTION_CONTENTS).withPotion(potion));
        }
        return stack;
    }
}
