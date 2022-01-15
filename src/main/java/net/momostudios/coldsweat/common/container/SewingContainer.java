package net.momostudios.coldsweat.common.container;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.config.ItemSettingsConfig;
import net.momostudios.coldsweat.core.init.ContainerInit;
import net.momostudios.coldsweat.core.init.BlockInit;
import net.momostudios.coldsweat.core.util.MathHelperCS;

public class SewingContainer extends Container
{
    private final IWorldPosCallable canInteractWithCallable;
    private IItemHandler itemHandler;
    private final SewingInventory inventory = new SewingInventory();

    public SewingContainer(final int windowId, final PlayerInventory playerInv, IWorldPosCallable canInteractWithCallable)
    {
        super(ContainerInit.SEWING_CONTAINER_TYPE.get(), windowId);
        this.canInteractWithCallable = canInteractWithCallable;
        itemHandler = new ItemStackHandler(3);

        // Input 1
        this.addSlot(new Slot(inventory, 0, 43, 26)
        {
            @Override
            public boolean isItemValid(ItemStack stack)
            {
                return stack.getItem() instanceof ArmorItem &&
                       !SewingContainer.this.isInsulatingItem(stack) &&
                       !stack.getOrCreateTag().getBoolean("insulated");
            }
            @Override
            public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack)
            {
                if (this.getStack().isEmpty())
                    SewingContainer.this.takeInput();
                return stack;
            }
            @Override
            public void onSlotChanged()
            {
                SewingContainer.this.testForRecipe();
            }
        });

        // Input 2
        this.addSlot(new Slot(inventory, 1, 43, 53)
        {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return SewingContainer.this.isInsulatingItem(stack);
            }
            @Override
            public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack)
            {
                if (this.getStack().isEmpty())
                    SewingContainer.this.takeInput();
                return stack;
            }
            @Override
            public void onSlotChanged()
            {
                SewingContainer.this.testForRecipe();
            }
        });

        // Output
        this.addSlot(new Slot(inventory, 2, 121, 39)
        {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack)
            {
                SewingContainer.this.takeOutput();
                return stack;
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

    private void takeInput()
    {
        this.getSlot(2).putStack(ItemStack.EMPTY);
    }
    private void takeOutput()
    {
        this.getSlot(0).getStack().shrink(1);
        this.getSlot(1).getStack().shrink(1);
    }
    private ItemStack testForRecipe()
    {
        ItemStack slot0Item = this.getSlot(0).getStack();
        ItemStack slot1Item = this.getSlot(1).getStack();
        ItemStack slot2Item = this.getSlot(2).getStack();
        ItemStack result = ItemStack.EMPTY;

        // Insulated Armor
        if (slot0Item.getItem() instanceof ArmorItem && this.isInsulatingItem(slot1Item) &&
            // Do slot types match OR is insulating item NOT armor
            (!(slot1Item.getItem() instanceof ArmorItem) ||
                    ((ArmorItem) slot0Item.getItem()).getEquipmentSlot().equals(((ArmorItem) slot1Item.getItem()).getEquipmentSlot())))
        {
            ItemStack processed = this.getSlot(0).getStack().copy();
            processed.getOrCreateTag().putBoolean("insulated", true);
            this.getSlot(2).putStack(processed);
            result = processed;
        }
        return result;
    }

    public SewingContainer(final int windowId, final PlayerInventory playerInv, final PacketBuffer data)
    {
        this(windowId, playerInv, IWorldPosCallable.DUMMY);
    }

    public boolean isInsulatingItem(ItemStack item)
    {
        for (String iterator : new ItemSettingsConfig().insulatingItems())
        {
            if (new ResourceLocation(iterator).equals(ForgeRegistries.ITEMS.getKey(item.getItem())))
            {
                return true;
            }
        }
        return false;
    }

    public static class SewingInventory implements IInventory
    {
        private final NonNullList<ItemStack> stackList;

        public SewingInventory()
        {
            this.stackList = NonNullList.withSize(3, ItemStack.EMPTY);
        }

        @Override
        public int getSizeInventory() {
            return 3;
        }

        @Override
        public boolean isEmpty() {
            return stackList.isEmpty();
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            return index >= this.getSizeInventory() ? ItemStack.EMPTY : stackList.get(index);
        }

        @Override
        public ItemStack decrStackSize(int index, int count)
        {
            ItemStack itemstack = ItemStackHelper.getAndSplit(this.stackList, index, count);

            return itemstack;
        }

        @Override
        public ItemStack removeStackFromSlot(int index) {
            return ItemStackHelper.getAndRemove(this.stackList, index);
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            this.stackList.set(index, stack);
        }

        @Override
        public void markDirty() {}

        @Override
        public boolean isUsableByPlayer(PlayerEntity player) {
            return true;
        }

        @Override
        public void clear() {
            this.stackList.clear();
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        PlayerInventory playerinventory = playerIn.inventory;
        if (!playerinventory.getItemStack().isEmpty())
        {
            playerIn.dropItem(playerinventory.getItemStack(), false);
            playerinventory.setItemStack(ItemStack.EMPTY);
        }

        for (int i = 0; i < this.inventory.getSizeInventory(); i++)
        {
            ItemStack itemStack = this.inventory.getStackInSlot(i);
            if (i != 2)
                if (!playerinventory.addItemStackToInventory(itemStack))
                {
                    ItemEntity itementity = playerinventory.player.dropItem(itemStack, false);
                    if (itementity != null)
                    {
                        itementity.setNoPickupDelay();
                        itementity.setOwnerId(playerinventory.player.getUniqueID());
                    }
                }
        }

    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return isWithinUsableDistance(canInteractWithCallable, playerIn, BlockInit.SEWING_TABLE.get());
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            itemstack = slot.getStack().copy();
            // Take from either input
            if ((MathHelperCS.isBetween(index, 0, 1)) && slot.inventory instanceof SewingInventory)
            {
                if (playerIn.inventory.addItemStackToInventory(itemstack))
                {
                    slot.putStack(ItemStack.EMPTY);
                    slot.onTake(playerIn, itemstack);
                }
            }
            // Take from output
            else if (slot.getSlotIndex() == 2 && slot.inventory instanceof SewingInventory)
            {
                for (int i = this.getSlot(0).getStack().getCount(); i > 0; i--)
                {
                    if (playerIn.addItemStackToInventory(itemstack))
                    {
                        slot.putStack(ItemStack.EMPTY);
                        slot.onTake(playerIn, itemstack);
                        this.testForRecipe();
                    }
                    else break;
                }
            }
            // Put into slot 1 (insulation item)
            else if (this.getSlot(1).isItemValid(itemstack) && this.inventory.getStackInSlot(1).isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
                this.inventory.setInventorySlotContents(1, itemstack);
                this.getSlot(1).onSlotChanged();
            }
            // Put into slot 0 (base item)
            else if (this.getSlot(0).isItemValid(itemstack) && this.inventory.getStackInSlot(0).isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
                this.inventory.setInventorySlotContents(0, itemstack);
                this.getSlot(0).onSlotChanged();
            }
            else if (MathHelperCS.isBetween(index, 3, 29))
            {
                for (int i = 30; i < 38; i++)
                {
                    Slot slot1 = playerIn.container.getSlot(i);
                    if (slot1.isItemValid(itemstack) && slot1.getStack().isEmpty())
                    {
                        if (itemstack.getCount() > slot1.getSlotStackLimit())
                        {
                            slot1.putStack(itemstack.split(slot1.getSlotStackLimit()));
                            slot1.onSlotChanged();
                            itemstack.shrink(slot1.getSlotStackLimit());
                        }
                        else
                        {
                            slot1.putStack(itemstack);
                            slot1.onSlotChanged();
                            slot.putStack(ItemStack.EMPTY);
                        }
                        break;
                    }
                }
            }
            else if (MathHelperCS.isBetween(index, 30, 38))
            {
                for (int i = 3; i < 29; i++)
                {
                    Slot slot1 = playerIn.container.getSlot(i);
                    if (slot1.isItemValid(itemstack) && slot1.getStack().isEmpty())
                    {
                        if (itemstack.getCount() > slot1.getSlotStackLimit())
                        {
                            slot1.putStack(itemstack.split(slot1.getSlotStackLimit()));
                            slot1.onSlotChanged();
                            itemstack.shrink(slot1.getSlotStackLimit());
                        }
                        else
                        {
                            slot1.putStack(itemstack);
                            slot1.onSlotChanged();
                            slot.putStack(ItemStack.EMPTY);
                        }
                        break;
                    }
                }
            }
            else
            {
                return ItemStack.EMPTY;
            }
        }
        else
        {
            return ItemStack.EMPTY;
        }
        return itemstack;
    }
}
