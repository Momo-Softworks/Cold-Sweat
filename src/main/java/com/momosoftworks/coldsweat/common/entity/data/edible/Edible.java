package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;

public abstract class Edible
{
    public abstract int getCooldown();

    public abstract Result onEaten(ChameleonEntity entity, ItemEntity item);

    public abstract boolean shouldEat(ChameleonEntity entity, ItemEntity item);

    public abstract ITag.INamedTag<Item> associatedItems();

    public String getName()
    {   return this.associatedItems().getName().toString();
    }

    public enum Result
    {
        SUCCESS,
        FAIL
    }
}
