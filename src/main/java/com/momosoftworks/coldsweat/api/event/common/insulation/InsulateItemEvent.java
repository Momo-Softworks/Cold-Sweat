package com.momosoftworks.coldsweat.api.event.common.insulation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when insulation is applied to an armor item.<br>
 * <br>
 * Cancelling this event will prevent the item from being insulated.
 */
public class InsulateItemEvent extends Event implements ICancellableEvent
{
    private final ItemStack armorItem;
    private ItemStack insulator;
    private final Player player;

    public InsulateItemEvent(ItemStack armorItem, ItemStack insulator, Player player)
    {
        this.armorItem = armorItem;
        this.insulator = insulator;
        this.player = player;
    }

    public ItemStack getArmorItem()
    {   return armorItem;
    }

    public ItemStack getInsulator()
    {   return insulator;
    }

    public void setInsulator(ItemStack insulator)
    {   this.insulator = insulator;
    }

    public Player getPlayer()
    {   return player;
    }
}
