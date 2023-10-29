package com.momosoftworks.coldsweat.util.world;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemHelper
{
    public static NBTTagCompound getOrCrateTag(ItemStack stack)
    {
        if (stack == null) return new NBTTagCompound();
        if (stack.hasTagCompound())
        {   return stack.getTagCompound();
        }
        else
        {   NBTTagCompound tag = new NBTTagCompound();
            stack.setTagCompound(tag);
            return tag;
        }
    }

    /**
     * Changes the size of the stack by the given amount. <br>
     * You should set the contents of the slot to the resulting stack if the resulting size can be 0.
     * @return The new ItemStack. If the size is 0 or less, null is returned.
     */
    public static ItemStack grow(ItemStack stack, int by)
    {   if (stack == null) return null;
        stack.stackSize += by;
        if (stack.stackSize <= 0)
        {   return null;
        }
        return stack;
    }

    public static boolean hasContainerItem(ItemStack stack)
    {   return stack.getItem().hasContainerItem(stack);
    }

    public static ItemStack getContainerItem(ItemStack stack)
    {   if (stack == null) return null;
        if (stack.getItem().hasContainerItem(stack))
        {   return stack.getItem().getContainerItem(stack);
        }
        return null;
    }
}
