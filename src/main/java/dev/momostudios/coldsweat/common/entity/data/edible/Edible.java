package dev.momostudios.coldsweat.common.entity.data.edible;

import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public abstract class Edible
{
    public abstract int getCooldown();

    public abstract Result onEaten(ChameleonEntity entity, ItemEntity item);

    public enum Result
    {
        SUCCESS,
        FAIL
    }
}
