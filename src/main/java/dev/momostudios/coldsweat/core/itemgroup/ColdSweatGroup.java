package dev.momostudios.coldsweat.core.itemgroup;

import dev.momostudios.coldsweat.util.registrylists.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

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
