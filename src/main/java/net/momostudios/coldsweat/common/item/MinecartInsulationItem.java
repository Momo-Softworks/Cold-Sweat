package net.momostudios.coldsweat.common.item;

import net.minecraft.item.Item;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;

public class MinecartInsulationItem extends Item
{
    public MinecartInsulationItem()
    {
        super(new Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(1));
    }
}
