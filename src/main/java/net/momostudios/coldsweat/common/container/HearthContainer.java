package net.momostudios.coldsweat.common.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.momostudios.coldsweat.common.item.FilledWaterskinItem;
import net.momostudios.coldsweat.common.te.HearthTileEntity;
import net.momostudios.coldsweat.core.init.ContainerInit;
import net.momostudios.coldsweat.core.init.ModBlocks;
import net.momostudios.coldsweat.core.util.ModItems;

import java.util.Objects;

public class HearthContainer extends Container
{
    public final HearthTileEntity te;
    private final IWorldPosCallable canInteractWithCallable;
    public HearthContainer(final int windowId, final PlayerInventory playerInv, final HearthTileEntity te)
    {
        super(ContainerInit.HEARTH_CONTAINER_TYPE.get(), windowId);
        this.te = te;
        this.canInteractWithCallable = IWorldPosCallable.of(te.getWorld(), te.getPos());
        int slotIndex = 0;

        // Tile Entity
        this.addSlot(new Slot((IInventory) te, 0, 80, 48)
        {
            @Override
            public boolean isItemValid(ItemStack stack)
            {
                if (!te.getFuelItem(stack).isEmpty())
                    return (int) te.getFuelItem(stack).get(1) != 0;
                else return false;
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

    public HearthContainer(final int windowId, final PlayerInventory playerInv, final PacketBuffer data)
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

    private static HearthTileEntity getTileEntity(final PlayerInventory playerInv, final PacketBuffer data)
    {
        Objects.requireNonNull(playerInv, "Player inventory cannot be null");
        Objects.requireNonNull(data, "PacketBuffer inventory cannot be null");
        final TileEntity te = playerInv.player.world.getTileEntity(data.readBlockPos());
        if (te instanceof HearthTileEntity)
        {
            return (HearthTileEntity) te;
        }
        throw new IllegalStateException("Tile Entity is not correct");
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return isWithinUsableDistance(canInteractWithCallable, playerIn, ModBlocks.HEARTH.get());
    }

    @Override
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
            else if (index > 0)
            {
                if (!this.te.getFuelItem(itemstack).isEmpty())
                {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
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
    }
}
