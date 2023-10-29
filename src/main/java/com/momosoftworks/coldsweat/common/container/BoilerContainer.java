package com.momosoftworks.coldsweat.common.container;

import com.momosoftworks.coldsweat.common.tileentity.BoilerTileEntity;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BoilerContainer extends Container
{
    private BoilerTileEntity te;
    private int lastFuel;

    public BoilerContainer(InventoryPlayer playerInv, BoilerTileEntity te)
    {   this.te = te;

        // Fuel slot
        this.addSlotToContainer(new Slot(te, 0, 80, 62)
        {
            @Override
            public boolean isItemValid(ItemStack stack)
            {   return te.getItemFuel(stack) > 0;
            }
        });

        // Waterskins
        for (int in = 1; in < 10; in++)
        {
            this.addSlotToContainer(new Slot(te, in, -10 + in * 18, 35)
            {
                @Override
                public boolean isItemValid(ItemStack stack)
                {   return stack.getItem() == ModItems.FILLED_WATERSKIN;
                }
            });
        }

        // Main player inventory
        for (int row = 0; row < 3; row++)
        {   for (int col = 0; col < 9; col++)
            {   this.addSlotToContainer(new Slot(playerInv, col + (9 * row) + 9, 8 + col * 18, 163 - (4 - row) * 18));
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; col++)
        {   this.addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 149));
        }
    }

    public int getFuel()
    {   return this.te.getFuel();
    }

    @Override
    public void addCraftingToCrafters(ICrafting craft)
    {   super.addCraftingToCrafters(craft);
        craft.sendProgressBarUpdate(this, 0, te.getFuel());
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
        for (Object obj : this.crafters)
        {   ICrafting craft = (ICrafting) obj;
            if (this.lastFuel != te.getFuel())
            {   craft.sendProgressBarUpdate(this, 0, te.getFuel());
            }
        }
        this.lastFuel = te.getFuel();
    }

    @Override
    public void updateProgressBar(int stat, int newValue)
    {   if (stat == 0)
        {   te.setFuel(newValue);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {   return te.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            // GUI slots
            if (CSMath.withinRange(index, 0, 9))
            {
                if (!this.mergeItemStack(itemstack1, 10, 46, true))
                {   return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            }
            // Inventory Slots
            else
            {
                // Transfer to waterskin slots
                if (((Slot) this.inventorySlots.get(1)).isItemValid(itemstack))
                {
                    if (!this.mergeItemStack(itemstack1, 1, 10, false))
                    {   return null;
                    }
                }
                // Transfer to fuel slot
                else if (((Slot) this.inventorySlots.get(0)).isItemValid(itemstack))
                {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false))
                    {   return null;
                    }
                }
                // Transfer from hotbar to main inventory
                else if (CSMath.withinRange(index, inventorySlots.size() - 9, inventorySlots.size()))
                {
                    if (!this.mergeItemStack(itemstack1, 10, 36, false))
                    {   return null;
                    }
                    slot.onSlotChange(itemstack1, itemstack);
                }
                // Transfer from main inventory to hotbar
                else if (CSMath.withinRange(index, 10, inventorySlots.size() - 9))
                {
                    if (!this.mergeItemStack(itemstack1, inventorySlots.size() - 9, inventorySlots.size(), false))
                    {   return null;
                    }
                }
            }

            if (itemstack1.stackSize == 0)
            {   slot.putStack(null);
            }
            else
            {   slot.onSlotChanged();
            }
            if (itemstack1.stackSize == itemstack.stackSize)
            {   return null;
            }

            slot.onPickupFromSlot(player, itemstack1);
        }

        return itemstack;
    }
}
