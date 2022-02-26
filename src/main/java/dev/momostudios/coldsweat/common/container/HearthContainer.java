package dev.momostudios.coldsweat.common.container;

import dev.momostudios.coldsweat.core.init.ContainerInit;
import net.minecraft.network.FriendlyByteBuf;
import dev.momostudios.coldsweat.common.te.HearthBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class HearthContainer extends AbstractContainerMenu
{
    public final HearthBlockEntity te;
    public HearthContainer(final int windowId, final Inventory playerInv, final HearthBlockEntity te)
    {
        super(ContainerInit.HEARTH_CONTAINER_TYPE.get(), windowId);
        this.te = te;

        // Tile Entity
        this.addSlot(new Slot(te, 0, 80, 48)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return te.getItemFuel(stack) != 0;
            }
        });

        // Main player inventory
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                this.addSlot(new Slot(playerInv, col + (9 * row) + 9, 8 + col * 18, 166 - (4 - row) * 18 - 10));
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; col++)
        {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public HearthContainer(final int windowId, final Inventory playerInv, final FriendlyByteBuf data)
    {
        this(windowId, playerInv, getTileEntity(playerInv, data));
    }

    public int getHotFuel()
    {
        return te.getTileData().getInt("hot_fuel");
    }

    public int getColdFuel()
    {
        return te.getTileData().getInt("cold_fuel");
    }

    private static HearthBlockEntity getTileEntity(final Inventory playerInv, final FriendlyByteBuf data)
    {
        Objects.requireNonNull(playerInv, "Player inventory cannot be null");
        Objects.requireNonNull(data, "PacketBuffer inventory cannot be null");
        final BlockEntity te = playerInv.player.level.getBlockEntity(data.readBlockPos());
        if (te instanceof HearthBlockEntity hearth)
        {
            return hearth;
        }
        throw new IllegalStateException("Tile Entity is not correct");
    }

    @Override
    public boolean stillValid(Player playerIn)
    {
        return playerIn.distanceToSqr(Vec3.atCenterOf(te.getBlockPos())) <= 64.0D;
    }

    /*@Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 1, 37, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else
            {
                if (this.te.getItemFuel(itemstack)!= 0)
                {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (CSMath.isBetween(index, inventorySlots.size() - 9, inventorySlots.size() - 1))
                {
                    if (!this.mergeItemStack(itemstack1, 1, inventorySlots.size() - 10, false))
                    {
                        slot.onSlotChange(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                else if (CSMath.isBetween(index, 1, inventorySlots.size() - 10))
                {
                    if (!this.mergeItemStack(itemstack1, inventorySlots.size() - 9, inventorySlots.size(), false))
                    {
                        slot.onSlotChange(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }*/
}
