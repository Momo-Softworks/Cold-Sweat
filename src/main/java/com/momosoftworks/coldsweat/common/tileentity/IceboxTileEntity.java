package com.momosoftworks.coldsweat.common.tileentity;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.IceboxBlock;
import com.momosoftworks.coldsweat.common.container.IceboxContainer;
import com.momosoftworks.coldsweat.core.init.ParticleTypesInit;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.BlockDataUpdateMessage;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.registries.ModBlockEntities;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class IceboxTileEntity extends LockableLootTileEntity implements ITickableTileEntity, ISidedInventory
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};

    public static int SLOTS = 10;
    public static int MAX_FUEL = 1000;

    LazyOptional<? extends IItemHandler>[] slotHandlers =
            SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    List<ServerPlayerEntity> usingPlayers = new ArrayList<>();
    public int ticksExisted;
    int fuel;

    public IceboxTileEntity()
    {   super(ModBlockEntities.ICEBOX);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag()
    {   CompoundNBT tag = super.getUpdateTag();
        tag.putInt("fuel", this.getFuel());
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag)
    {   this.setFuel(tag.getInt("fuel"));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        handleUpdateTag(null, pkt.getTag());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {   return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    private void sendUpdatePacket()
    {
        // Remove the players that aren't interacting with this block anymore
        usingPlayers.removeIf(player -> !(player.containerMenu instanceof IceboxContainer && ((IceboxContainer) player.containerMenu).te == this));

        // Send data to all players with this block's menu open
        ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.NMLIST.with(()-> usingPlayers.stream().map(player -> player.connection.connection).collect(Collectors.toList())),
                                             new BlockDataUpdateMessage(this));
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container." + ColdSweat.MOD_ID + ".icebox");
    }

    @Override
    public void tick()
    {
        ticksExisted++;

        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();

        if (!level.isClientSide)
        {
            if (getFuel() > 0)
            {
                // Set state to frosted
                if (!state.getValue(IceboxBlock.FROSTED))
                    level.setBlock(pos, state.setValue(IceboxBlock.FROSTED, true), 3);

                // Cool down waterskins
                if (ticksExisted % 20 == 0)
                {
                    boolean hasItemStacks = false;
                    for (int i = 1; i < 10; i++)
                    {
                        ItemStack stack = getItem(i);
                        int itemTemp = stack.getOrCreateTag().getInt("temperature");

                        if (stack.getItem() == ModItems.FILLED_WATERSKIN && itemTemp > -50)
                        {
                            hasItemStacks = true;
                            stack.getOrCreateTag().putInt("temperature", itemTemp - 1);
                        }
                    }
                    if (hasItemStacks) setFuel(getFuel() - 1);
                }
            }
            // if no fuel, set state to unfrosted
            else if (state.getValue(IceboxBlock.FROSTED))
            {
                level.setBlock(pos, state.setValue(IceboxBlock.FROSTED, false), 3);
            }

            // Input fuel
            if (this.ticksExisted % 10 == 0)
            {
                ItemStack fuelStack = this.getItem(0);
                int itemFuel = getItemFuel(fuelStack);

                if (itemFuel != 0 && this.getFuel() < MAX_FUEL - itemFuel / 2)
                {
                    if (fuelStack.hasContainerItem() && fuelStack.getCount() == 1)
                    {
                        this.setItem(0, fuelStack.getContainerItem());
                        setFuel(this.getFuel() + itemFuel);
                    }
                    else
                    {
                        int consumeCount = Math.min((int) Math.floor((MAX_FUEL - fuel) / (double) Math.abs(itemFuel)), fuelStack.getCount());
                        fuelStack.shrink(consumeCount);
                        setFuel(this.getFuel() + itemFuel * consumeCount);
                    }
                }
            }
        }

        if (state.getValue(IceboxBlock.FROSTED) && ticksExisted % 3 == 0 && Math.random() < 0.5)
        {
            double d0 = pos.getX() + 0.5;
            double d1 = pos.getY();
            double d2 = pos.getZ() + 0.5;
            boolean side = new Random().nextBoolean();
            double d5 = side ? Math.random() - 0.5 : (Math.random() < 0.5 ? 0.55 : -0.55);
            double d6 = Math.random() * 0.3;
            double d7 = !side ? Math.random() - 0.5 : (Math.random() < 0.5 ? 0.55 : -0.55);
            level.addParticle(ParticleTypesInit.MIST.get(), d0 + d5, d1 + d6, d2 + d7, d5 / 40, 0.0D, d7 / 40);
        }
    }

    public int getItemFuel(ItemStack item)
    {
        return ConfigSettings.ICEBOX_FUEL.get().getOrDefault(item.getItem(), 0d).intValue();
    }

    public int getFuel()
    {
        return fuel;
    }

    public void setFuel(int amount)
    {
        fuel = Math.min(amount, MAX_FUEL);
        if (this.level != null && !this.level.isClientSide)
        {
            this.sendUpdatePacket();
        }
    }

    @Override
    protected Container createMenu(int id, PlayerInventory playerInv)
    {   // Track the players using this block
        if (playerInv.player instanceof ServerPlayerEntity)
        {   usingPlayers.add((ServerPlayerEntity) playerInv.player);
        }
        return new IceboxContainer(id, playerInv, this);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag)
    {
        super.load(state, tag);
        this.setFuel(tag.getInt("fuel"));
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, this.items);
    }

    @Override
    public CompoundNBT save(CompoundNBT tag)
    {
        super.save(tag);
        tag.putInt("fuel", this.getFuel());
        ItemStackHelper.saveAllItems(tag, this.items);
        return tag;
    }

    @Override
    public int getContainerSize()
    {
        return SLOTS;
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
    public void setItem(int slot, ItemStack itemstack)
    {
        items.set(slot, itemstack);
    }

    @Override
    public ItemStack removeItem(int slot, int count)
    {
        ItemStack itemstack = ItemStackHelper.removeItem(items, slot, count);

        if (!itemstack.isEmpty())
        {   this.setChanged();
        }
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {   return ItemStackHelper.removeItem(items, slot, items.get(slot).getCount());
    }

    @Override
    public boolean stillValid(PlayerEntity player)
    {   return player.distanceToSqr(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent()
    {
        items.clear();
    }

    @Override
    protected NonNullList<ItemStack> getItems()
    {   return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items)
    {   this.items = items;
    }

    @Override
    public int[] getSlotsForFace(Direction dir)
    {
        return dir.getAxis() == Direction.Axis.Y ? WATERSKIN_SLOTS : FUEL_SLOT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction)
    {
        if (slot == 0)
            return this.getItemFuel(stack) != 0;
        else return stack.getItem() == ModItems.WATERSKIN || stack.getItem() == ModItems.FILLED_WATERSKIN;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction)
    {
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == Direction.UP)
                return slotHandlers[0].cast();
            else if (facing == Direction.DOWN)
                return slotHandlers[1].cast();
            else
                return slotHandlers[2].cast();
        }
        return super.getCapability(capability, facing);
    }
}
