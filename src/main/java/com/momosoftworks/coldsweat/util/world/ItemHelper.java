package com.momosoftworks.coldsweat.util.world;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemHelper
{
    public static final ItemStack EMPTY_STACK = new ItemStack(Blocks.air);

    public static NBTTagCompound getOrCrateTag(ItemStack stack)
    {   if (stack.hasTagCompound())
        {   return stack.getTagCompound();
        }
        else
        {   NBTTagCompound tag = new NBTTagCompound();
            stack.setTagCompound(tag);
            return tag;
        }
    }
}
