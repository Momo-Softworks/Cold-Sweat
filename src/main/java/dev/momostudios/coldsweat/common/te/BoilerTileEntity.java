package dev.momostudios.coldsweat.common.te;

import dev.momostudios.coldsweat.util.registrylists.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.block.BoilerBlock;
import dev.momostudios.coldsweat.common.container.BoilerContainer;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;

import javax.annotation.Nullable;
import java.util.List;

public class BoilerTileEntity extends BaseContainerBlockEntity
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};
    public static int slots = 10;
    public static int MAX_FUEL = 1000;
    protected NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);
    public int ticksExisted;
    private int fuel;

    public BoilerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }


    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container." + ColdSweat.MOD_ID + ".boiler");
    }

    public void tick()
    {
        this.ticksExisted++;
        this.ticksExisted %= 1000;

        if (!this.level.isClientSide)
        {
            if (this.getFuel() > 0)
            {
                if (!level.getBlockState(getBlockPos()).getValue(BoilerBlock.LIT))
                    level.setBlock(getBlockPos(), level.getBlockState(getBlockPos()).setValue(BoilerBlock.LIT, true), 3);

                if (this.ticksExisted % 20 == 0)
                {
                    boolean hasItemStacks = false;
                    for (int i = 0; i < 10; i++)
                    {
                        if (this.getItemInSlot(i).getItem() == ModItems.FILLED_WATERSKIN && this.getItemInSlot(i).getOrCreateTag().getInt("temperature") < 50)
                        {
                            hasItemStacks = true;
                            this.getItemInSlot(i).getOrCreateTag().putInt("temperature", this.getItemInSlot(i).getOrCreateTag().getInt("temperature") + 1);
                        }
                    }
                    if (hasItemStacks) this.setFuel(this.getFuel() - 1);
                }
            }
            else if (level.getBlockState(getBlockPos()).getValue(BoilerBlock.LIT))
            {
                level.setBlock(getBlockPos(), level.getBlockState(getBlockPos()).setValue(BoilerBlock.LIT, false), 3);
            }

            int itemFuel = getItemFuel(this.getItemInSlot(0));
            if (itemFuel > 0)
            {
                ItemStack item = this.getItemInSlot(0);
                if (this.getFuel() <= MAX_FUEL - itemFuel * 0.75)
                {
                    if (item.hasContainerItem())
                    {
                        this.setItemInSlot(0, item.getContainerItem());
                    }
                    else
                    {
                        this.getItemInSlot(0).shrink(1);
                    }

                    this.setFuel(this.getFuel() + itemFuel);
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
        return this.fuel;
    }

    public void setFuel(int amount)
    {
        fuel = Math.min(amount, MAX_FUEL);
    }

    public LazyOptional<IItemHandler> getCap()
    {
        return this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    public int[] getSlotsForFace(Direction side)
    {
        if (side == Direction.DOWN)
        {
            return WATERSKIN_SLOTS;
        }
        else
        {
            return side == Direction.UP ? WATERSKIN_SLOTS : FUEL_SLOT;
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
    {
        return new BoilerContainer(id, playerInv, this, getFuel());
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.setFuel(nbt.getInt("fuel"));
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt("fuel", this.getFuel());
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
}
