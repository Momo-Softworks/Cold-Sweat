package dev.momostudios.coldsweat.common.blockentity;

import dev.momostudios.coldsweat.util.registries.ModBlockEntities;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.block.BoilerBlock;
import dev.momostudios.coldsweat.common.container.BoilerContainer;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;

import java.util.List;

public class BoilerBlockEntity extends RandomizableContainerBlockEntity implements MenuProvider
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};
    public static int slots = 10;
    public static int MAX_FUEL = 1000;
    protected NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);
    public int ticksExisted;

    public BoilerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.get("boiler"), pos, state);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("fuel", this.getFuel());
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container." + ColdSweat.MOD_ID + ".boiler");
    }

    @Override
    public Component getDisplayName() {
        return this.getCustomName() != null ? this.getCustomName() : this.getDefaultName();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T te)
    {
        if (te instanceof BoilerBlockEntity boilerTE)
        {
            boilerTE.ticksExisted++;
            boilerTE.ticksExisted %= 1000;

            if (boilerTE.getFuel() > 0)
            {
                if (!state.getValue(BoilerBlock.LIT))
                    level.setBlock(pos, state.setValue(BoilerBlock.LIT, true), 3);

                if (boilerTE.ticksExisted % 20 == 0)
                {
                    boolean hasItemStacks = false;
                    for (int i = 0; i < 10; i++)
                    {
                        if (boilerTE.getItemInSlot(i).getItem() == ModItems.FILLED_WATERSKIN && boilerTE.getItemInSlot(i).getOrCreateTag().getInt("temperature") < 50)
                        {
                            hasItemStacks = true;
                            boilerTE.getItemInSlot(i).getOrCreateTag().putInt("temperature", boilerTE.getItemInSlot(i).getOrCreateTag().getInt("temperature") + 1);
                        }
                    }
                    if (hasItemStacks) boilerTE.setFuel(boilerTE.getFuel() - 1);
                }
            }
            else if (state.getValue(BoilerBlock.LIT))
            {
                level.setBlock(pos, state.setValue(BoilerBlock.LIT, false), 3);
            }

            int itemFuel = boilerTE.getItemFuel(boilerTE.getItemInSlot(0));
            if (itemFuel > 0)
            {
                ItemStack item = boilerTE.getItemInSlot(0);
                if (boilerTE.getFuel() <= MAX_FUEL - itemFuel * 0.75)
                {
                    if (item.hasContainerItem())
                    {
                        boilerTE.setItemInSlot(0, item.getContainerItem());
                    }
                    else
                    {
                        boilerTE.getItemInSlot(0).shrink(1);
                    }

                    boilerTE.setFuel(boilerTE.getFuel() + itemFuel);
                }
            }
        }
    }

    public int getItemFuel(ItemStack item)
    {
        int fuel = 0;
        for (List<?> testIndex : new ItemSettingsConfig().boilerItems())
        {
            String testItem = (String) testIndex.get(0);

            if (new ResourceLocation(testItem).equals(ForgeRegistries.ITEMS.getKey(item.getItem())))
            {
                fuel = ((Number) testIndex.get(1)).intValue();
                break;
            }
        }
        return fuel;
    }

    public ItemStack getItemInSlot(int index)
    {
        return getCap().map(c -> c.getStackInSlot(index)).orElse(ItemStack.EMPTY);
    }

    public void setItemInSlot(int index, ItemStack stack)
    {
        getCap().ifPresent(capability ->
        {
            capability.getStackInSlot(index).shrink(capability.getStackInSlot(index).getCount());
            capability.insertItem(index, stack, false);
        });
    }

    public int getFuel()
    {
        return this.getTileData().getInt("fuel");
    }

    public void setFuel(int amount)
    {
        this.getTileData().putInt("fuel", Math.min(amount, MAX_FUEL));
    }

    public LazyOptional<IItemHandler> getCap()
    {
        return this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
    {
        return new BoilerContainer(id, playerInv, this);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.setFuel(tag.getInt("fuel"));
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("fuel", this.getFuel());
    }

    @Override
    public int getContainerSize()
    {
        return slots;
    }

    @Override
    public boolean isEmpty()
    {
        return items.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot)
    {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count)
    {
        ItemStack itemstack = ContainerHelper.removeItem(items, slot, count);

        if (!itemstack.isEmpty())
        {
            this.setChanged();
        }
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        return ContainerHelper.removeItem(items, slot, items.get(slot).getCount());
    }

    @Override
    public void setItem(int slot, ItemStack itemstack)
    {
        items.set(slot, itemstack);
    }

    @Override
    public boolean stillValid(Player player)
    {
        return player.distanceToSqr(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent()
    {
        items.clear();
    }

    @Override
    protected NonNullList<ItemStack> getItems()
    {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items)
    {
        this.items = items;
    }
}
