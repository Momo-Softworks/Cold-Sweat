package dev.momostudios.coldsweat.common.entity.data.edible;

import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import net.minecraft.world.item.ItemStack;

public abstract class Edible
{
    public abstract int getCooldown();

    public void onEaten(ChameleonEntity entity, ItemStack stack)
    {
        entity.setCooldown(stack.getItem(), this.getCooldown());
    }
}
