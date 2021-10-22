package net.momostudios.coldsweat.common.item;

import net.minecraft.item.Item;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;

public class SoulfireLampItem extends Item
{
    public SoulfireLampItem()
    {
        super(new Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(1));
    }
}
