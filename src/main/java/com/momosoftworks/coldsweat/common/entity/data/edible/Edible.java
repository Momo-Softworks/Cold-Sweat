package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.momosoftworks.coldsweat.common.entity.Chameleon;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;

public abstract class Edible
{
    public abstract int getCooldown();

    public abstract Result onEaten(Chameleon entity, ItemEntity item);

    public abstract boolean shouldEat(Chameleon entity, ItemEntity item);

    public abstract TagKey<Item> associatedItems();

    public String getName()
    {
        return this.associatedItems().location().toString();
    }

    public enum Result
    {
        SUCCESS,
        FAIL
    }
}
