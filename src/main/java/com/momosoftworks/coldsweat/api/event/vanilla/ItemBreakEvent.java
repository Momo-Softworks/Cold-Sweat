package com.momosoftworks.coldsweat.api.event.vanilla;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.function.Consumer;

/**
 * Generic event that fires when an ItemStack is broken in {@link ItemStack#hurtAndBreak(int, LivingEntity, Consumer)}<br>
 * <b>The entity CAN BE NULL!</b>
 */
public class ItemBreakEvent extends LivingEvent
{
    private final ItemStack itemStack;
    private final Consumer<LivingEntity> onBroken;

    public ItemBreakEvent(ItemStack itemStack, LivingEntity entity, Consumer<LivingEntity> onBroken)
    {
        super(entity);
        this.itemStack = itemStack;
        this.onBroken = onBroken;
    }

    public ItemStack getItemStack()
    {   return itemStack;
    }

    public Consumer<LivingEntity> getBrokenCallback()
    {   return onBroken;
    }
}
