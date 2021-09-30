package net.momostudios.coldsweat.common.te;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.container.IceboxContainer;
import net.momostudios.coldsweat.config.FuelItemsConfig;
import net.momostudios.coldsweat.core.init.TileEntityInit;
import net.momostudios.coldsweat.core.util.ModItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class IceboxTileEntity extends LockableLootTileEntity implements ITickableTileEntity
{
    public static int slots = 10;
    protected NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);
    public int ticksExisted;

    public IceboxTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container." + ColdSweat.MOD_ID + ".icebox");
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

    public IceboxTileEntity()
    {
        this(TileEntityInit.ICEBOX_TILE_ENTITY_TYPE.get());
    }

    public void tick()
    {
        this.ticksExisted++;
        this.ticksExisted %= 1000;

        if (this.getFuel() > 0)
        {
            if (this.ticksExisted % 20 == 0)
            {
                boolean hasItemStacks = false;
                for (int i = 0; i < 10; i++) {
                    if (this.getItemInSlot(i).getItem() == ModItems.FILLED_WATERSKIN && this.getItemInSlot(i).getOrCreateTag().getInt("temperature") > -50)
                    {
                        hasItemStacks = true;
                        this.getItemInSlot(i).getOrCreateTag().putInt("temperature", this.getItemInSlot(i).getOrCreateTag().getInt("temperature") - 1);
                    }
                }
                if (hasItemStacks) this.setFuel(this.getFuel() - 1);
            }
        }

        if (!getFuelItem(this.getItemInSlot(9)).isEmpty())
        {
            ItemStack fuel = this.getItemInSlot(9);
            int amount = (int) getFuelItem(this.getItemInSlot(9)).get(1);
            if (this.getFuel() <= Math.max(1000 - amount, 900))
            {
                if (fuel.hasContainerItem())
                {
                    this.setItemInSlot(9, fuel.getContainerItem());
                }
                else this.getItemInSlot(9).shrink(1);
                this.setFuel(this.getFuel() + amount);
            }
        }
    }

    public List getFuelItem(ItemStack item)
    {
        List returnList = new ArrayList();
        for (Object iterator : FuelItemsConfig.getInstance().iceboxItems())
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
        AtomicReference<ItemStack> stack = new AtomicReference<>(ItemStack.EMPTY);
        this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability ->
        {
            stack.set(capability.getStackInSlot(index));
        });
        return stack.get();
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

    @Override
    public int getSizeInventory() {
        return slots;
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player)
    {
        return new IceboxContainer(id, player, this);
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
