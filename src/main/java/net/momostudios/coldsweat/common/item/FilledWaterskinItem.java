package net.momostudios.coldsweat.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntNBT;
import net.minecraft.world.World;
import net.momostudios.coldsweat.common.temperature.PlayerTemp;
import net.momostudios.coldsweat.common.temperature.modifier.WaterskinTempModifier;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.init.ModItems;

public class FilledWaterskinItem extends Item
{
    public FilledWaterskinItem()
    {
        super(new Item.Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(1).containerItem(ModItems.WATERSKIN.get()));
    }

    @Override
    public void inventoryTick(ItemStack itemstack, World world, Entity entity, int slot, boolean selected)
    {
        super.inventoryTick(itemstack, world, entity, slot, selected);
        if (entity instanceof PlayerEntity)
        {
            int itemTemp = itemstack.getOrCreateTag().getInt("temperature");
            if (entity.ticksExisted % 30 == 0 && itemTemp > 0 && ((slot <= 8 && slot >= 0) || slot == -106))
            {
                if (itemTemp > 0)
                    itemstack.getOrCreateTag().putInt("temperature", itemTemp - 1);
                else if (itemTemp < 0)
                    itemstack.getOrCreateTag().putInt("temperature", itemTemp + 1);

                PlayerTemp.applyModifier((PlayerEntity) entity, new WaterskinTempModifier(), PlayerTemp.Types.BODY, false, IntNBT.valueOf(itemTemp));
            }
        }
    }

}
