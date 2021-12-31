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
import net.momostudios.coldsweat.common.te.BoilerTileEntity;
import net.momostudios.coldsweat.core.init.BlockInit;
import net.momostudios.coldsweat.core.init.ContainerInit;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.registrylists.ModItems;

import java.util.Objects;

public class BoilerContainer extends Container
{
    public final BoilerTileEntity te;
    private final IWorldPosCallable canInteractWithCallable;
    public BoilerContainer(final int windowId, final PlayerInventory playerInv, final BoilerTileEntity te)
    {
        super(ContainerInit.BOILER_CONTAINER_TYPE.get(), windowId);
        this.te = te;
        this.canInteractWithCallable = IWorldPosCallable.of(te.getWorld(), te.getPos());

        // Tile Entity
        this.addSlot(new Slot(te, 0, 80, 62)
        {
            @Override
            public boolean isItemValid(ItemStack stack)
            {
                return !te.getFuelItem(stack).isEmpty();
            }
        });

        for (int in = 1; in < 10; in++)
        {
            this.addSlot(new Slot(te, in, -10 + in * 18, 35)
            {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem() instanceof FilledWaterskinItem;
                }
            });
        }

        // Main player inventory
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                this.addSlot(new Slot(playerInv, col + (9 * row) + 9, 8 + col * 18, 163 - (4 - row) * 18));
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; col++)
        {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 149));
        }
    }

    public BoilerContainer(final int windowId, final PlayerInventory playerInv, final PacketBuffer data)
    {
        this(windowId, playerInv, getTileEntity(playerInv, data));
    }

    public int getFuel()
    {
        return te.getTileData().getInt("fuel");
    }


    private static BoilerTileEntity getTileEntity(final PlayerInventory playerInv, final PacketBuffer data)
    {
        Objects.requireNonNull(playerInv, "Player inventory cannot be null");
        Objects.requireNonNull(data, "PacketBuffer inventory cannot be null");
        final TileEntity te = playerInv.player.world.getTileEntity(data.readBlockPos());
        if (te instanceof BoilerTileEntity)
        {
            return (BoilerTileEntity) te;
        }
        throw new IllegalStateException("Tile Entity is not correct");
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return isWithinUsableDistance(canInteractWithCallable, playerIn, BlockInit.BOILER.get());
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
            if (MathHelperCS.isBetween(index, 0, 9))
            {
                if (!this.mergeItemStack(itemstack1, 10, 46, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else
            {
                if (itemstack.getItem() == ModItems.FILLED_WATERSKIN)
                {
                    if (!this.mergeItemStack(itemstack1, 1, 10, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (!this.te.getFuelItem(itemstack).isEmpty())
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

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }
}
