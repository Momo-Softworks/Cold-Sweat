package com.momosoftworks.coldsweat.core.itemgroup;

import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ColdSweatGroup extends ItemGroup
{
    public static final ColdSweatGroup COLD_SWEAT = new ColdSweatGroup("cold_sweat");

    public ColdSweatGroup(String label)
    {   super(label);
    }

    @Override
    public ItemStack makeIcon()
    {   return FilledWaterskinItem.getDisplayStack();
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> items)
    {
        ModItems.WATERSKIN.fillItemCategory(this, items);
        ModItems.FILLED_WATERSKIN.fillItemCategory(this, items);
        ModItems.GOAT_FUR.fillItemCategory(this, items);
        ModItems.HOGLIN_HIDE.fillItemCategory(this, items);
        ModItems.CHAMELEON_MOLT.fillItemCategory(this, items);
        ModItems.MINECART_INSULATION.fillItemCategory(this, items);
        ModItems.INSULATED_MINECART.fillItemCategory(this, items);
        ModItems.SOULSPRING_LAMP.fillItemCategory(this, items);
        ModItems.SOUL_SPROUT.fillItemCategory(this, items);
        ModItems.THERMOMETER.fillItemCategory(this, items);
        ModItems.THERMOLITH.fillItemCategory(this, items);
        ModItems.HEARTH.fillItemCategory(this, items);
        ModItems.BOILER.fillItemCategory(this, items);
        ModItems.ICEBOX.fillItemCategory(this, items);
        ModItems.SMOKESTACK.fillItemCategory(this, items);
        ModItems.SEWING_TABLE.fillItemCategory(this, items);
        ModItems.HOGLIN_HELMET.fillItemCategory(this, items);
        ModItems.HOGLIN_CHESTPLATE.fillItemCategory(this, items);
        ModItems.HOGLIN_LEGGINGS.fillItemCategory(this, items);
        ModItems.HOGLIN_BOOTS.fillItemCategory(this, items);
        ModItems.GOAT_FUR_HELMET.fillItemCategory(this, items);
        ModItems.GOAT_FUR_CHESTPLATE.fillItemCategory(this, items);
        ModItems.GOAT_FUR_LEGGINGS.fillItemCategory(this, items);
        ModItems.GOAT_FUR_BOOTS.fillItemCategory(this, items);
        ModItems.CHAMELEON_HELMET.fillItemCategory(this, items);
        ModItems.CHAMELEON_CHESTPLATE.fillItemCategory(this, items);
        ModItems.CHAMELEON_LEGGINGS.fillItemCategory(this, items);
        ModItems.CHAMELEON_BOOTS.fillItemCategory(this, items);
    }
}
