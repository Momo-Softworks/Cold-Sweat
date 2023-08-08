package dev.momostudios.coldsweat.core.itemgroup;

import dev.momostudios.coldsweat.util.registries.ModItems;
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
    {   return new ItemStack(ModItems.FILLED_WATERSKIN);
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> items)
    {
        ModItems.WATERSKIN.fillItemCategory(this, items);
        ModItems.FILLED_WATERSKIN.fillItemCategory(this, items);
        ModItems.LLAMA_FUR.fillItemCategory(this, items);
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
        ModItems.SEWING_TABLE.fillItemCategory(this, items);
        ModItems.HOGLIN_HEADPIECE.fillItemCategory(this, items);
        ModItems.HOGLIN_TUNIC.fillItemCategory(this, items);
        ModItems.HOGLIN_TROUSERS.fillItemCategory(this, items);
        ModItems.HOGLIN_HOOVES.fillItemCategory(this, items);
        ModItems.GOAT_FUR_CAP.fillItemCategory(this, items);
        ModItems.GOAT_FUR_PARKA.fillItemCategory(this, items);
        ModItems.GOAT_FUR_PANTS.fillItemCategory(this, items);
        ModItems.GOAT_FUR_BOOTS.fillItemCategory(this, items);
    }
}
