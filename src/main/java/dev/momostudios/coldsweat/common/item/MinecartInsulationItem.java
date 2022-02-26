package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.minecraft.world.item.Item;

public class MinecartInsulationItem extends Item
{
    public MinecartInsulationItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1));
    }
}
