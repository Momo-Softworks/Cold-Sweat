package com.momosoftworks.coldsweat.api.event.vanilla;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class EntityPickEvent extends Event
{
    private final Entity entity;
    private ItemStack stack;

    public EntityPickEvent(Entity entity, ItemStack result)
    {
        this.entity = entity;
        this.stack = result;
    }

    public Entity getEntity()
    {
        return entity;
    }

    public ItemStack getStack()
    {
        return stack;
    }

    public void setStack(ItemStack stack)
    {
        this.stack = stack;
    }
}
