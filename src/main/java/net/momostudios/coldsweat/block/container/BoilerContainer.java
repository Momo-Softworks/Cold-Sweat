package net.momostudios.coldsweat.block.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.momostudios.coldsweat.block.tileentity.BoilerTileEntity;
import net.momostudios.coldsweat.util.init.ModBlocks;
import net.momostudios.coldsweat.util.init.ContainerInit;

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
        int slotIndex = 0;

        // Tile Entity
        for (int in = 0; in < 9; in++)
        {
            this.addSlot(new Slot((IInventory) te, in, 8 + in * 18, 35));
        }

        this.addSlot(new Slot((IInventory) te, 9, 80, 62));

        // Main player inventory
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                this.addSlot(new Slot(playerInv, col + (9 * row) + 9, 8 + col * 18, 202 - (4 - row) * 18 - 10));
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; col++)
        {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 178));
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
        return isWithinUsableDistance(canInteractWithCallable, playerIn, ModBlocks.BOILER.get());
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            ItemStack stack1 = slot.getStack();
            stack = stack1.copy();
            if (index < 36 && !this.mergeItemStack(stack1, BoilerTileEntity.slots, this.inventorySlots.size(), true))
            {
                return ItemStack.EMPTY;
            }
            if (!this.mergeItemStack(stack1, 0, BoilerTileEntity.slots, false))
            {
                return ItemStack.EMPTY;
            }
            if (stack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

        }
        return stack;
    }
}
