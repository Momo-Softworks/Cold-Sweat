package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;

public abstract class Edible
{
    public abstract int getCooldown();

    public abstract Result onEaten(ItemStack item, ChameleonEntity entity, Entity thrower);

    public abstract boolean shouldEat(ItemStack item, ChameleonEntity entity, Entity thrower);

    public abstract ITag.INamedTag<Item> associatedItems();

    public String getName()
    {   return this.associatedItems().getName().toString();
    }

    public enum Result
    {
        SUCCESS,
        FAIL,
        PASS;
    }
}
