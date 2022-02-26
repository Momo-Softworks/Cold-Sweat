package dev.momostudios.coldsweat.common.container;

import dev.momostudios.coldsweat.core.init.ContainerInit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;

public class SewingContainer extends AbstractContainerMenu
{
    BlockPos pos;

    public SewingContainer(final int windowId, final Inventory playerInv)
    {
        super(ContainerInit.SEWING_CONTAINER_TYPE.get(), windowId);

        // Input 1
        this.addSlot(new Slot(playerInv, 0, 43, 26)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return stack.getItem() instanceof ArmorItem &&
                       !SewingContainer.this.isInsulatingItem(stack) &&
                       !stack.getOrCreateTag().getBoolean("insulated");
            }
            @Override
            public void onTake(Player player, ItemStack stack)
            {
                if (this.getItem().isEmpty())
                    SewingContainer.this.takeInput();
            }
            @Override
            public void setChanged()
            {
                SewingContainer.this.testForRecipe();
            }
        });

        // Input 2
        this.addSlot(new Slot(playerInv, 1, 43, 53)
        {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SewingContainer.this.isInsulatingItem(stack);
            }
            @Override
            public void onTake(Player player, ItemStack stack)
            {
                if (this.getItem().isEmpty())
                    SewingContainer.this.takeInput();
            }
            @Override
            public void setChanged()
            {
                SewingContainer.this.testForRecipe();
            }
        });

        // Output
        this.addSlot(new Slot(playerInv, 2, 121, 39)
        {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player thePlayer, ItemStack stack)
            {
                SewingContainer.this.takeOutput();
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
        this.getSlot(2).set(ItemStack.EMPTY);
    }
    private void takeOutput()
    {
        this.getSlot(0).getItem().shrink(1);
        this.getSlot(1).getItem().shrink(1);
    }
    private ItemStack testForRecipe()
    {
        ItemStack slot0Item = this.getSlot(0).getItem();
        ItemStack slot1Item = this.getSlot(1).getItem();
        ItemStack result = ItemStack.EMPTY;

        // Insulated Armor
        if (slot0Item.getItem() instanceof ArmorItem && this.isInsulatingItem(slot1Item) &&
        // Do slot types match OR is insulating item NOT armor
        (!(slot1Item.getItem() instanceof ArmorItem) || slot0Item.getItem().getEquipmentSlot(slot0Item).equals(slot1Item.getItem().getEquipmentSlot(slot1Item))))
        {
            ItemStack processed = this.getSlot(0).getItem().copy();
            processed.getOrCreateTag().putBoolean("insulated", true);
            this.getSlot(2).set(processed);
            result = processed;
        }
        return result;
    }

    public SewingContainer(final int windowId, final Player playerInv, BlockPos pos)
    {
        this(windowId, playerInv.getInventory());
        this.pos = pos;
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

    @Override
    public void removed(Player playerIn)
    {
        Inventory playerinventory = playerIn.getInventory();
        playerIn.drop(getCarried(), false);
        setCarried(ItemStack.EMPTY);

        for (int i = 0; i < 3; i++)
        {
            ItemStack itemStack = this.getSlot(i).getItem();
            if (i != 2)
                if (!playerinventory.add(itemStack))
                {
                    ItemEntity itementity = playerinventory.player.drop(itemStack, false);
                    if (itementity != null)
                    {
                        itementity.setNoPickUpDelay();
                        itementity.setOwner(playerinventory.player.getUUID());
                    }
                }
        }

    }

    @Override
    public boolean stillValid(Player playerIn)
    {
        return playerIn.distanceToSqr(Vec3.atCenterOf(this.pos)) <= 64.0D;
    }

    /*@Override
    public ItemStack transferStackInSlot(Player playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (CSMath.isBetween(index, 0, 2))
            {
                if (!this.mergeItemStack(itemstack1, 3, 39, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else
            {
                if (isInsulatingItem(itemstack1))
                {
                    if (!this.mergeItemStack(itemstack1, 1, 2, false))
                    {
                        slot.onSlotChange(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                else if (itemstack1.getItem() instanceof ArmorItem)
                {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false))
                    {
                        slot.onSlotChange(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                else if (index == 2)
                {
                    if (!this.mergeItemStack(itemstack1, 3, 39, false))
                    {
                        slot.onSlotChange(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                else if (CSMath.isBetween(index, inventorySlots.size() - 9, inventorySlots.size()))
                {
                    if (!this.mergeItemStack(itemstack1, 3, 29, false))
                    {
                        slot.onSlotChange(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                else if (CSMath.isBetween(index, 3, inventorySlots.size() - 9))
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

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }*/
}
