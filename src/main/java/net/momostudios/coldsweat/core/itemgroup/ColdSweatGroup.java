package net.momostudios.coldsweat.core.itemgroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.momostudios.coldsweat.core.init.ItemInit;
import net.momostudios.coldsweat.core.util.ModItems;

public class ColdSweatGroup extends ItemGroup
{
    public static final ColdSweatGroup COLD_SWEAT = new ColdSweatGroup(ItemGroup.GROUPS.length, "cold_sweat");
    public ColdSweatGroup(int index, String label)
    {
        super(index, label);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(ModItems.FILLED_WATERSKIN);
    }
}
