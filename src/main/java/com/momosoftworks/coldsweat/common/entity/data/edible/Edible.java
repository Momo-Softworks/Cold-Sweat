package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.momosoftworks.coldsweat.common.entity.Chameleon;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.valkyrienskies.core.impl.shadow.En;

public abstract class Edible
{
    public abstract int getCooldown();

    public abstract Result onEaten(ItemStack item, Chameleon entity, Entity thrower);

    public abstract boolean shouldEat(ItemStack item, Chameleon entity, Entity thrower);

    public abstract TagKey<Item> associatedItems();

    public String getName()
    {
        return this.associatedItems().location().toString();
    }

    public enum Result
    {
        SUCCESS,
        FAIL,
        PASS;
    }
}
