package dev.momostudios.coldsweat.core.itemgroup;

import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ColdSweatGroup extends CreativeModeTab
{
    public static final ColdSweatGroup COLD_SWEAT = new ColdSweatGroup("cold_sweat");
    public ColdSweatGroup(String label)
    {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ModItems.FILLED_WATERSKIN);
    }
}
