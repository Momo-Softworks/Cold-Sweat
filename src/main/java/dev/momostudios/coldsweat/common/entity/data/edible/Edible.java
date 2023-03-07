package dev.momostudios.coldsweat.common.entity.data.edible;

import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public abstract class Edible
{
    public abstract int getCooldown();

    public void onEaten(ChameleonEntity entity, ItemEntity item)
    {
        entity.setCooldown(item.getItem().getItem(), this.getCooldown());
    }
}
