package dev.momostudios.coldsweat.common.container;

import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.core.init.MenuInit;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SewingContainer extends AbstractContainerMenu
{
    BlockPos pos;
    Inventory playerInventory;
    SewingInventory sewingInventory;
    public static List<Item> VALID_INSULATORS = new ArrayList<>();
    static
    {
        for (String itemID : ItemSettingsConfig.getInstance().insulatingItems())
        {
            VALID_INSULATORS.addAll(ConfigHelper.getItems(itemID));
        }
    }

    public static class SewingInventory implements Container
    {
        private final NonNullList<ItemStack> stackList;
        private final AbstractContainerMenu menu;

        public SewingInventory(AbstractContainerMenu menu)
        {
            this.stackList = NonNullList.withSize(3, ItemStack.EMPTY);
            this.menu = menu;
        }

        @Override
        public int getContainerSize()
        {
            return 3;
        }

        @Override
        public boolean isEmpty()
        {
            return !stackList.stream().anyMatch(stack -> !stack.isEmpty());
        }

        @Nonnull
        @Override
        public ItemStack getItem(int index)
        {
            return stackList.get(index);
        }

        @Nonnull
        @Override
        public ItemStack removeItem(int index, int count)
        {
            ItemStack itemstack = ContainerHelper.removeItem(this.stackList, index, count);
            if (!itemstack.isEmpty()) {
                this.menu.slotsChanged(this);
            }

            return itemstack;
        }

        @Nonnull
        @Override
        public ItemStack removeItemNoUpdate(int index)
        {
            return ContainerHelper.takeItem(this.stackList, index);
        }

        @Override
        public void setItem(int index, ItemStack stack)
        {
            this.stackList.set(index, stack);
            this.menu.slotsChanged(this);
        }

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(Player player)
        {
            return true;
        }

        @Override
        public void clearContent()
        {
            stackList.clear();
        }
    }

    public SewingContainer(final int windowId, final Inventory playerInv)
    {
        super(MenuInit.SEWING_CONTAINER_TYPE.get(), windowId);
        this.pos = playerInv.player.blockPosition();
        this.playerInventory = playerInv;
        sewingInventory = new SewingInventory(this);

        // Input 1
        this.addSlot(new Slot(sewingInventory, 0, 43, 26)
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
                super.setChanged();
                SewingContainer.this.testForRecipe();
            }
        });

        // Input 2
        this.addSlot(new Slot(sewingInventory, 1, 43, 53)
        {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SewingContainer.this.isInsulatingItem(stack);
            }
            @Override
            public void onTake(Player player, ItemStack stack)
            {
                SewingContainer.this.takeInput();
            }
            @Override
            public void setChanged()
            {
                super.setChanged();
                SewingContainer.this.testForRecipe();
            }
        });

        // Output
        this.addSlot(new Slot(sewingInventory, 2, 121, 39)
        {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack)
            {
                super.onTake(player, stack);
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

    public SewingContainer(int i, Inventory inventory, FriendlyByteBuf friendlyByteBuf)
    {
        this(i, inventory);
        try {
            this.pos = BlockPos.of(friendlyByteBuf.readLong());
        } catch (Exception e) {}
    }

    private void takeInput()
    {
        this.sewingInventory.setItem(2, ItemStack.EMPTY);
    }
    private void takeOutput()
    {
        this.sewingInventory.getItem(0).shrink(1);
        this.setRemoteSlot(0, this.sewingInventory.getItem(0));

        this.sewingInventory.getItem(1).shrink(1);
        this.setRemoteSlot(1, this.sewingInventory.getItem(1));

        Player player = this.playerInventory.player;
        SoundEvent equipSound = this.getCarried().getItem().getEquipSound();
        if (equipSound != null)
        {
            player.level.playSound(null, player.blockPosition(), equipSound, SoundSource.BLOCKS, 1f, 1f);
        }
        player.level.playSound(null, player.blockPosition(), SoundEvents.LLAMA_SWAG, SoundSource.BLOCKS, 0.5f, 1f);
    }
    private ItemStack testForRecipe()
    {
        ItemStack slot0Item = this.sewingInventory.getItem(0);
        ItemStack slot1Item = this.sewingInventory.getItem(1);
        ItemStack result = ItemStack.EMPTY;

        // Insulated Armor
        if (slot0Item.getItem() instanceof ArmorItem && this.isInsulatingItem(slot1Item) &&
        // Do slot types match OR is insulating item NOT armor
        (!(slot1Item.getItem() instanceof ArmorItem) || LivingEntity.getEquipmentSlotForItem(slot0Item).equals(LivingEntity.getEquipmentSlotForItem(slot1Item))))
        {
            ItemStack processed = slot0Item.copy();
            processed.getOrCreateTag().putBoolean("insulated", true);
            this.sewingInventory.setItem(2, processed);
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
        return VALID_INSULATORS.contains(item.getItem());
    }

    @Override
    public void removed(Player playerIn)
    {
        Inventory playerinventory = playerIn.getInventory();
        playerIn.drop(getCarried(), false);
        setCarried(ItemStack.EMPTY);

        // Drop the contents of the input slots
        for (int i = 0; i < 2; i++)
        {
            ItemStack itemStack = this.getSlot(i).getItem();
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
        if (this.pos != null)
            return playerIn.distanceToSqr(Vec3.atCenterOf(this.pos)) <= 64.0D;
        else return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem())
        {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (CSMath.isInRange(index, 0, 2))
            {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            }
            else
            {
                if (isInsulatingItem(itemstack1))
                {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false))
                    {
                        slot.onQuickCraft(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                else if (itemstack1.getItem() instanceof ArmorItem)
                {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false))
                    {
                        slot.onQuickCraft(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                else if (index == 2)
                {
                    if (!this.moveItemStackTo(itemstack1, 3, 39, false))
                    {
                        slot.onQuickCraft(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                else if (CSMath.isInRange(index, slots.size() - 9, slots.size()))
                {
                    if (!this.moveItemStackTo(itemstack1, 3, 29, false))
                    {
                        slot.onQuickCraft(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                else if (CSMath.isInRange(index, 3, slots.size() - 9))
                {
                    if (!this.moveItemStackTo(itemstack1, slots.size() - 9, slots.size(), false))
                    {
                        slot.onQuickCraft(itemstack1, itemstack);
                        return ItemStack.EMPTY;
                    }
                }
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}
