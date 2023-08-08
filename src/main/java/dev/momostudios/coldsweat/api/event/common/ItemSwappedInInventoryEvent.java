package dev.momostudios.coldsweat.api.event.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ItemSwappedInInventoryEvent extends Event
{
    final ItemStack slotItem;
    final ItemStack heldItem;
    final Container container;
    final PlayerEntity player;

    public ItemSwappedInInventoryEvent(ItemStack slotItem, ItemStack heldItem, Container container, PlayerEntity player)
    {
        this.slotItem = slotItem;
        this.heldItem = heldItem;
        this.container = container;
        this.player = player;
    }

    public ItemStack getSlotItem()
    {   return slotItem;
    }

    public ItemStack getHeldItem()
    {   return heldItem;
    }

    public Container getContainer()
    {   return container;
    }

    public PlayerEntity getPlayer()
    {   return player;
    }
}
