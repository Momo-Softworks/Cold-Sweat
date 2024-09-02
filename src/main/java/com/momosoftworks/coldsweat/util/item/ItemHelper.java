package com.momosoftworks.coldsweat.util.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;

public class ItemHelper
{
    public static boolean isEffectivelyPickaxe(ItemStack stack)
    {
        if (stack.getItem() instanceof PickaxeItem)
        {   return true;
        }

        // Check if the item can mine stone efficiently
        BlockState stoneState = Blocks.STONE.defaultBlockState();
        return stack.isCorrectToolForDrops(stoneState);
    }
}
