package com.momosoftworks.coldsweat.api.event.vanilla;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import java.util.function.Consumer;

/**
 * Generic event that fires when an ItemStack is broken in {@link net.minecraft.world.item.ItemStack#hurtAndBreak(int, ServerLevel, LivingEntity, Consumer)} 
 * <b>The entity CAN BE NULL!</b>
 */
public class ItemBreakEvent extends LivingEvent
{
    private final ItemStack itemStack;
    private final Consumer<Item> onBroken;

    public ItemBreakEvent(ItemStack itemStack, LivingEntity entity, Consumer<Item> onBroken)
    {
        super(entity);
        this.itemStack = itemStack;
        this.onBroken = onBroken;
    }

    public ItemStack getItemStack()
    {   return itemStack;
    }

    public Consumer<Item> getBrokenCallback()
    {   return onBroken;
    }
}
