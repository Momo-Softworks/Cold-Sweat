package net.momostudios.coldsweat.common.item;

import net.minecraft.item.Item;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.init.ModItems;

public class FilledWaterskinItem
{
    public static Item.Properties getProperties()
    {
        return new Item.Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(1).containerItem(ModItems.WATERSKIN.get());
    }
}
