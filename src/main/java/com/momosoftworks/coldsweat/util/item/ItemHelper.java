package com.momosoftworks.coldsweat.util.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;

public class ItemHelper
{
    public static boolean isEffectivelyPickaxe(ItemStack stack)
    {
        if (stack.getItem() instanceof PickaxeItem)
        {   return true;
        }

        // Check if the item can mine stone efficiently
        BlockState stoneState = Blocks.STONE.defaultBlockState();
        return stack.isCorrectToolForDrops(stoneState)
            && stack.getItem() instanceof TieredItem item
            && TierSortingRegistry.isCorrectTierForDrops(item.getTier(), stoneState);
    }
}
