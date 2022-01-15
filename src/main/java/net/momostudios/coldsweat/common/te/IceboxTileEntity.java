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
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.block.IceboxBlock;
import net.momostudios.coldsweat.common.container.IceboxContainer;
import net.momostudios.coldsweat.config.ItemSettingsConfig;
import net.momostudios.coldsweat.core.init.ParticleTypesInit;
import net.momostudios.coldsweat.core.init.TileEntityInit;
import net.momostudios.coldsweat.core.util.registrylists.ModItems;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class IceboxTileEntity extends LockableLootTileEntity implements ITickableTileEntity, ISidedInventory
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};
    public static int slots = 10;
    protected NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);
    public int ticksExisted;
    private int fuel;

    protected final IIntArray fuelData = new IIntArray() {
        public int get(int index) {
            return fuel;
        }

        public void set(int index, int value) {
            fuel = value;
        }

        public int size() {
            return 1;
        }
    };

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

        if (!this.world.isRemote)
        {
            if (this.getFuel() > 0)
            {
                if (!world.getBlockState(pos).get(IceboxBlock.FROSTED))
                    world.setBlockState(pos, world.getBlockState(pos).with(IceboxBlock.FROSTED, true));

                if (this.ticksExisted % 20 == 0)
                {
                    boolean hasItemStacks = false;
                    for (int i = 0; i < 10; i++)
                    {
                        if (this.getItemInSlot(i).getItem() == ModItems.FILLED_WATERSKIN && this.getItemInSlot(i).getOrCreateTag().getInt("temperature") > -50)
                        {
                            hasItemStacks = true;
                            this.getItemInSlot(i).getOrCreateTag().putInt("temperature", this.getItemInSlot(i).getOrCreateTag().getInt("temperature") - 1);
                        }
                    }
                    if (hasItemStacks) this.setFuel(this.getFuel() - 1);
                }
            }
            else
            {
                if (world.getBlockState(pos).get(IceboxBlock.FROSTED))
                    world.setBlockState(pos, world.getBlockState(pos).with(IceboxBlock.FROSTED, false));
            }

            int itemFuel = getItemFuel(this.getItemInSlot(0));
            if (itemFuel > 0)
            {
                ItemStack item = this.getItemInSlot(0);
                if (this.getFuel() <= 1000 - itemFuel * 0.75)
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
        else
        {
            if (world.getBlockState(pos).get(IceboxBlock.FROSTED) && world.getGameTime() % 3 == 0 &&  Math.random() < 0.5)
            {
                double d0 = pos.getX() + 0.5;
                double d1 = pos.getY();
                double d2 = pos.getZ() + 0.5;
                boolean side = new Random().nextBoolean();
                double d5 = side ? Math.random() * 2 - 1 : (Math.random() < 0.5 ? 0.55 : -0.55);
                double d6 = Math.random() * 0.3;
                double d7 = !side ? Math.random() * 2 - 1 : (Math.random() < 0.5 ? 0.55 : -0.55);
                world.addParticle(ParticleTypesInit.MIST.get(), d0 + d5, d1 + d6, d2 + d7, d5 / 40, 0.0D, d7 / 40);
            }
        }
    }

    public int getItemFuel(ItemStack item)
    {
        int fuel = 0;
        for (List<String> testIndex : new ItemSettingsConfig().iceboxItems())
        {
            String testItem = testIndex.get(0);

            if (new ResourceLocation(testItem).equals(ForgeRegistries.ITEMS.getKey(item.getItem())))
            {
                fuel = Integer.parseInt(testIndex.get(1));
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
        return fuelData.get(0);
    }

    public void setFuel(int amount)
    {
        fuelData.set(0, Math.min(amount, 1000));
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
    public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction)
    {
        return index == 0 || getCap().map(h -> canInsertItem(index, itemStackIn, direction)).orElse(false);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction)
    {
        return getItemFuel(stack) == 0;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    @Override
    public int getSizeInventory() {
        return slots;
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player)
    {
        return new IceboxContainer(id, player, this, fuelData);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        this.setFuel(nbt.getInt("fuel"));
        this.items = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(nbt, this.items);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);
        compound.putInt("fuel", this.getFuel());
        ItemStackHelper.saveAllItems(compound, items);
        return compound;
    }
}
