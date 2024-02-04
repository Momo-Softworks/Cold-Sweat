package com.momosoftworks.coldsweat.common.container;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.common.capability.IInsulatableCap;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationCap;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationManager;
import com.momosoftworks.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ContainerInit;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.IArmorVanishable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SewingContainer extends Container
{
    BlockPos pos;
    PlayerInventory playerInventory;
    SewingInventory sewingInventory;

    public static class SewingInventory implements IInventory
    {
        private final NonNullList<ItemStack> stackList;
        private final Container menu;

        public SewingInventory(Container menu)
        {   this.stackList = NonNullList.withSize(3, ItemStack.EMPTY);
            this.menu = menu;
        }

        public int getContainerSize()
        {   return 3;
        }

        public boolean isEmpty()
        {   return stackList.stream().allMatch(ItemStack::isEmpty);
        }

        public ItemStack getItem(int index)
        {   return stackList.get(index);
        }

        @Override
        public ItemStack removeItem(int index, int count)
        {
            ItemStack itemstack = ItemStackHelper.removeItem(this.stackList, index, count);
            if (!itemstack.isEmpty())
            {   this.menu.slotsChanged(this);
            }

            return itemstack;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index)
        {   return ItemStackHelper.takeItem(this.stackList, index);
        }

        @Override
        public void setItem(int index, ItemStack stack)
        {   this.stackList.set(index, stack);
            this.menu.slotsChanged(this);
        }

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(PlayerEntity player)
        {   return true;
        }

        @Override
        public void clearContent()
        {   stackList.clear();
        }
    }

    public SewingContainer(final int windowId, final PlayerInventory playerInv)
    {
        super(ContainerInit.SEWING_CONTAINER_TYPE.get(), windowId);
        this.pos = playerInv.player.blockPosition();
        this.playerInventory = playerInv;
        sewingInventory = new SewingInventory(this);

        // Input 1
        this.addSlot(new Slot(sewingInventory, 0, 43, 26)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {   Pair<Double, Double> insulation = ItemInsulationManager.getItemInsulation(stack);
                return stack.getItem() instanceof IArmorVanishable && !ConfigSettings.INSULATION_BLACKLIST.get().contains(ForgeRegistries.ITEMS.getKey(stack.getItem()))
                    && insulation.getFirst() == 0 && insulation.getSecond() == 0;
            }

            @Override
            public ItemStack onTake(PlayerEntity player, ItemStack stack)
            {   super.onTake(player, stack);
                SewingContainer.this.takeInput();
                return stack;
            }

            @Override
            public void setChanged()
            {   super.setChanged();
                SewingContainer.this.testForRecipe();
            }
        });

        // Input 2
        this.addSlot(new Slot(sewingInventory, 1, 43, 53)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {   Pair<Double, Double> insulation = ItemInsulationManager.getItemInsulation(stack);
                return insulation.getFirst() != 0 || insulation.getSecond() != 0 || stack.getItem() instanceof ShearsItem;
            }

            @Override
            public ItemStack onTake(PlayerEntity player, ItemStack stack)
            {   super.onTake(player, stack);
                SewingContainer.this.takeInput();
                return stack;
            }

            @Override
            public void setChanged()
            {   super.setChanged();
                SewingContainer.this.testForRecipe();
            }
        });

        // Output
        this.addSlot(new Slot(sewingInventory, 2, 121, 39)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return false;
            }

            @Override
            public ItemStack onTake(PlayerEntity player, ItemStack stack)
            {
                super.onTake(player, stack);
                SewingContainer.this.takeOutput(stack);

                if (!SewingContainer.this.playerInventory.player.level.isClientSide)
                {   TaskScheduler.scheduleServer(() -> testForRecipe(), 1);
                }
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

    public SewingContainer(int i, PlayerInventory inventory, PacketBuffer friendlyByteBuf)
    {
        this(i, inventory);
        try {
            this.pos = BlockPos.of(friendlyByteBuf.readLong());
        } catch (Exception ignored) {}
    }

    public void setItem(int index, ItemStack stack)
    {
        if (index >= sewingInventory.stackList.size())
            return;
        this.sewingInventory.setItem(index, stack);
    }

    public void growItem(int index, int amount)
    {
        ItemStack stack = this.sewingInventory.getItem(index);
        stack.grow(amount);
        this.sewingInventory.setItem(index, stack);
    }

    public ItemStack getItem(int index)
    {
        return this.sewingInventory.getItem(index);
    }

    private void takeInput()
    {
        this.sewingInventory.setItem(2, ItemStack.EMPTY);
    }
    private void takeOutput(ItemStack stack)
    {
        PlayerEntity player = this.playerInventory.player;
        ItemStack input1 = this.getItem(0);
        ItemStack input2 = this.getItem(1);

        ItemInsulationManager.getInsulationCap(input1).ifPresent(cap ->
        {
            // If insulation is being removed
            if (this.getItem(1).getItem() instanceof ShearsItem)
            {
                if (!cap.getInsulation().isEmpty())
                {   // Damage shears
                    if (!player.isCreative())
                    {   input2.hurt(1, player.getRandom(), null);
                    }

                    // Remove the last insulation item added
                    cap.removeInsulationItem(cap.getInsulationItem(cap.getInsulation().size() - 1));
                    // Play shear sound
                    player.level.playSound(null, player.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundCategory.PLAYERS, 0.8F, 1.0F);

                    serializeInsulation(input1, cap);
                }
            }
            // If insulation is being added
            else
            {   // Remove input items
                this.growItem(0, -1);
                this.growItem(1, -1);
                player.level.playSound(null, player.blockPosition(), SoundEvents.LLAMA_SWAG, SoundCategory.BLOCKS, 0.5f, 1f);

                // Trigger advancement criteria
                if (player instanceof ServerPlayerEntity)
                    ModAdvancementTriggers.ARMOR_INSULATED.trigger(((ServerPlayerEntity) player), input1, input2);
            }
        });

        if (stack.getItem() instanceof ArmorItem)
        {
            ArmorItem armor = (ArmorItem) stack.getItem();
            // Get equip sound for the armor item
            SoundEvent equipSound = armor.getMaterial().getEquipSound();
            player.level.playSound(null, player.blockPosition(), equipSound, SoundCategory.BLOCKS, 1f, 1f);
        }
    }

    static void serializeInsulation(ItemStack stack, IInsulatableCap iCap)
    {
        if (iCap instanceof ItemInsulationCap)
        {
            ItemInsulationCap cap = (ItemInsulationCap) iCap;
            CompoundNBT tag = cap.serializeSimple(stack);

            // Remove "Insulation" tag if armor has no insulation left
            if (iCap.getInsulation().isEmpty())
            {   tag.remove("Insulation");
            }
        }
    }

    private void testForRecipe()
    {
        ItemStack wearableItem = this.getItem(0);
        ItemStack insulatorItem = this.getItem(1);

        // Is the first item armor, and the second item an insulator
        if (wearableItem.getItem() instanceof IArmorVanishable)
        {
            // Shears are used to remove insulation
            if (insulatorItem.getItem() instanceof ShearsItem)
            {
                ItemInsulationManager.getInsulationCap(wearableItem).ifPresent(cap ->
                {
                    if (!cap.getInsulation().isEmpty())
                    {   this.setItem(2, cap.getInsulationItem(cap.getInsulation().size() - 1).copy());
                    }
                });
            }
            // Item is for insulation
            else if ((ConfigSettings.INSULATION_ITEMS.get().containsKey(insulatorItem.getItem()) || ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().containsKey(insulatorItem.getItem()))
            && (!(insulatorItem.getItem() instanceof IArmorVanishable)
            || MobEntity.getEquipmentSlotForItem(wearableItem) == MobEntity.getEquipmentSlotForItem(insulatorItem)))
            {
                ItemStack processed = wearableItem.copy();
                IInsulatableCap insulCap = ItemInsulationManager.getInsulationCap(processed).orElseThrow(() -> new IllegalStateException("Item does not have insulation capability"));
                ItemStack insulator = insulatorItem.copy();
                insulator.setCount(1);
                insulCap.addInsulationItem(insulator);

                // Cancel crafting if the insulation provided by the insulator is too much
                AtomicInteger posInsul = new AtomicInteger();
                AtomicInteger negInsul = new AtomicInteger();
                // Get the total positive/negative insulation of the armor
                insulCap.getInsulation().stream().map(Pair::getSecond).flatMap(Collection::stream).forEach(pair ->
                {
                    if (pair instanceof ItemInsulationCap.Insulation)
                    {   ItemInsulationCap.Insulation insul = (ItemInsulationCap.Insulation) pair;
                        if (insul.getHot() > 0 || insul.getCold() > 0) posInsul.getAndIncrement();
                        else negInsul.getAndIncrement();
                    }
                    if (pair instanceof ItemInsulationCap.AdaptiveInsulation)
                    {   ItemInsulationCap.AdaptiveInsulation insul = (ItemInsulationCap.AdaptiveInsulation) pair;
                        if (insul.getInsulation() > 0) posInsul.getAndIncrement();
                        else negInsul.getAndIncrement();
                    }
                });
                if (posInsul.get() > ItemInsulationManager.getInsulationSlots(wearableItem) || negInsul.get() > ItemInsulationManager.getInsulationSlots(wearableItem))
                {   return;
                }

                // Transfer enchantments
                Map<Enchantment, Integer> armorEnch = EnchantmentHelper.getEnchantments(processed);
                insulator.getEnchantmentTags().removeIf(nbt ->
                {
                    CompoundNBT enchantTag = ((CompoundNBT) nbt);
                    Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantTag.getString("id")));
                    if (ench == null) return false;

                    if (ench.canEnchant(wearableItem) && armorEnch.keySet().stream().allMatch(ench2 -> ench2.isCompatibleWith(ench)))
                    {   processed.enchant(ench, enchantTag.getInt("lvl"));
                        return true;
                    }
                    return false;
                });

                serializeInsulation(processed, insulCap);

                this.setItem(2, processed);
            }
        }
    }

    public SewingContainer(final int windowId, final PlayerEntity player, BlockPos pos)
    {   this(windowId, player.inventory);
        this.pos = pos;
    }

    @Override
    public void removed(PlayerEntity player)
    {
        super.removed(player);

        // Drop the contents of the input slots
        if (player instanceof ServerPlayerEntity)
        {
            for (int i = 0; i < sewingInventory.getContainerSize(); i++)
            {
                ItemStack itemStack = this.sewingInventory.getItem(i);
                if (!itemStack.isEmpty() && i != 2)
                {
                    if (player.isAlive() && !((ServerPlayerEntity) player).hasDisconnected())
                    {   player.inventory.placeItemBackInInventory(player.level, itemStack);
                    }
                    else player.drop(itemStack, false, true);
                }
            }
        }
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn)
    {
        if (this.pos != null)
        {   return playerIn.distanceToSqr(Vector3d.atCenterOf(this.pos)) <= 64.0D;
        }
        else return true;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index)
    {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem())
        {
            ItemStack slotItem = slot.getItem();
            newStack = slotItem.copy();
            if (CSMath.isWithin(index, 0, 2))
            {
                if (this.moveItemStackTo(slotItem, 3, 39, true))
                {   slot.onTake(player, newStack);
                }
                else return ItemStack.EMPTY;
            }
            else
            {
                // Item is valid insulation
                if (this.slots.get(1).mayPlace(slotItem))
                {
                    if (this.moveItemStackTo(slotItem, 1, 2, false))
                    {   slot.onQuickCraft(slotItem, newStack);
                    }
                    else return ItemStack.EMPTY;
                }
                // Item is valid wearable
                else if (this.slots.get(0).mayPlace(slotItem))
                {
                    if (!this.moveItemStackTo(slotItem, 0, 1, false))
                    {   slot.onQuickCraft(slotItem, newStack);
                    }
                    else return ItemStack.EMPTY;
                }
                // Item is in hotbar
                else if (CSMath.isWithin(index, slots.size() - 9, slots.size()))
                {
                    if (!this.moveItemStackTo(slotItem, 3, 29, false))
                    {   slot.onQuickCraft(slotItem, newStack);
                    }
                    else return ItemStack.EMPTY;
                }
                // Item is in inventory
                else if (CSMath.isWithin(index, 3, slots.size() - 9))
                {
                    if (!this.moveItemStackTo(slotItem, slots.size() - 9, slots.size(), false))
                    {   slot.onQuickCraft(slotItem, newStack);
                    }
                    else return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty())
            {   slot.set(ItemStack.EMPTY);
            }
            else slot.setChanged();
        }

        return newStack;
    }
}
