package com.momosoftworks.coldsweat.compat.curios;

import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class EquipableCurio implements ICurioItem
{
    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack)
    {   return true;
    }
}
