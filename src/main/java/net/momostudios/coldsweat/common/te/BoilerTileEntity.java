package net.momostudios.coldsweat.common.te;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.block.BoilerBlock;
import net.momostudios.coldsweat.common.container.BoilerContainer;
import net.momostudios.coldsweat.config.ItemSettingsConfig;
import net.momostudios.coldsweat.core.init.TileEntityInit;
import net.momostudios.coldsweat.core.util.registrylists.ModItems;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoilerTileEntity extends LockableLootTileEntity implements ITickableTileEntity, ISidedInventory
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};
    public static int slots = 10;
    protected NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);
    public int ticksExisted;

    public BoilerTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container." + ColdSweat.MOD_ID + ".boiler");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn)
    {
        this.items = itemsIn;
    }

    public BoilerTileEntity()
    {
        this(TileEntityInit.BOILER_TILE_ENTITY_TYPE.get());
    }

    public void tick()
    {
        this.ticksExisted++;
        this.ticksExisted %= 1000;

        if (this.getFuel() > 0)
        {
            if (this.ticksExisted % 20 == 0)
            {
                world.setBlockState(pos, world.getBlockState(pos).with(BoilerBlock.LIT, true));
                boolean hasItemStacks = false;
                for (int i = 0; i < 10; i++) {
                    if (this.getItemInSlot(i).getItem() == ModItems.FILLED_WATERSKIN && this.getItemInSlot(i).getOrCreateTag().getInt("temperature") < 50)
                    {
                        hasItemStacks = true;
                        this.getItemInSlot(i).getOrCreateTag().putInt("temperature", this.getItemInSlot(i).getOrCreateTag().getInt("temperature") + 1);
                    }
                }
                if (hasItemStacks) this.setFuel(this.getFuel() - 1);
            }
        }
        else
        {
            world.setBlockState(pos, world.getBlockState(pos).with(BoilerBlock.LIT, false));
        }

        if (!getFuelItem(this.getItemInSlot(0)).isEmpty())
        {
            List fuelItem = getFuelItem(this.getItemInSlot(0));

            ItemStack fuel = (ItemStack) fuelItem.get(0);
            int amount = (int) fuelItem.get(1);
            if (this.getFuel() <= Math.max(1000 - amount, 900))
            {
                if (fuel.hasContainerItem())
                {
                    this.setItemInSlot(0, fuel.getContainerItem());
                }
                else this.getItemInSlot(0).shrink(1);
                this.setFuel(this.getFuel() + amount);
            }
        }
    }

    public List getFuelItem(ItemStack item)
    {
        List returnList = new ArrayList();
        for (Object iterator : new ItemSettingsConfig().boilerItems())
        {
            List<String> testIndex = (List<String>) iterator;
            String testItem = testIndex.get(0);

            if (new ResourceLocation(testItem).equals(ForgeRegistries.ITEMS.getKey(item.getItem())))
            {
                returnList = Arrays.asList(item, Integer.parseInt(testIndex.get(1)));
            }
        }
        return returnList;
    }

    public ItemStack getItemInSlot(int index)
    {
        return this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null).getStackInSlot(index);
    }

    public void setItemInSlot(int index, ItemStack stack)
    {
        this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability ->
        {
            if (stack != null && stack != capability.getStackInSlot(index))
            {
                capability.extractItem(index, capability.getStackInSlot(index).getCount(), false);
            }
            capability.insertItem(index, stack, false);
        });
    }

    public int getFuel()
    {
        return this.getTileData().getInt("fuel");
    }

    public void setFuel(int amount)
    {
        this.getTileData().putInt("fuel", Math.min(amount, 1000));
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
    public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction)
    {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction)
    {
        return index != 0;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return index == 0 ? !getFuelItem(stack).isEmpty() : stack.getItem() == ModItems.FILLED_WATERSKIN;
    }

    @Override
    public int getSizeInventory() {
        return slots;
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player)
    {
        return new BoilerContainer(id, player, this);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        this.items = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(nbt))
        {
            ItemStackHelper.loadAllItems(nbt, this.items);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);
        if (!this.checkLootAndWrite(compound))
        {
            ItemStackHelper.saveAllItems(compound, items);
        }
        return compound;
    }
}
