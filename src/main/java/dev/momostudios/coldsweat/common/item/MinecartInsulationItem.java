package dev.momostudios.coldsweat.common.item;

import net.minecraft.item.Item;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;

public class MinecartInsulationItem extends Item
{
    public MinecartInsulationItem()
    {
        super(new Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(1));
    }
}
