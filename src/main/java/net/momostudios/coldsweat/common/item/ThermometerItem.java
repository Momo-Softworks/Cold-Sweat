package net.momostudios.coldsweat.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.util.PlayerTemp;

public class ThermometerItem extends Item
{
    public ThermometerItem()
    {
        super(new Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(1).rarity(Rarity.UNCOMMON));
    }
}
