package com.momosoftworks.coldsweat.common.blockentity;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.BoilerBlock;
import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.BlockEntityInit;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import java.util.Arrays;
import java.util.List;

public class BoilerBlockEntity extends HearthBlockEntity implements ITickableTileEntity, ISidedInventory
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};

    protected boolean hasWaterskins = false;

    LazyOptional<? extends IItemHandler>[] slotHandlers =
            SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    public BoilerBlockEntity()
    {   super(BlockEntityInit.BOILER_BLOCK_ENTITY_TYPE.get());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {   handleUpdateTag(null, pkt.getTag());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {   return new SUpdateTileEntityPacket(this.getBlockPos(), 0, this.getUpdateTag());
    }

    @Override
    protected ITextComponent getDefaultName()
    {   return new TranslationTextComponent("container." + ColdSweat.MOD_ID + ".boiler");
    }

    @Override
    public void tick()
    {
        super.tick();

        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();

        if (this.getFuel() > 0)
        {
            if (this.ticksExisted % (int) (20 / Math.max(1, ConfigSettings.TEMP_RATE.get())) == 0)
            {
                // Warm up waterskins
                this.hasWaterskins = false;
                for (int i = 1; i < 10; i++)
                {
                    ItemStack stack = this.getItem(i);
                    CompoundNBT tag = NBTHelper.getTagOrEmpty(stack);
                    double itemTemp = tag.getDouble(FilledWaterskinItem.NBT_TEMPERATURE);

                    if (stack.getItem() == ModItems.FILLED_WATERSKIN && itemTemp < 50)
                    {   tag.putDouble(FilledWaterskinItem.NBT_TEMPERATURE, Math.min(50, itemTemp + 1));
                        hasWaterskins = true;
                    }
                }
                // Drain fuel
                if (this.hasWaterskins)
                {   this.setFuel(this.getFuel() - 1);
                }
            }
        }
        // Update lit state
        boolean shouldBeLit = this.getFuel() > 0 && (this.hasWaterskins || this.shouldUseHotFuel);
        if (state.getValue(BoilerBlock.LIT) != shouldBeLit)
        {   level.setBlock(pos, state.setValue(BoilerBlock.LIT, shouldBeLit), 3);
        }
    }

    public void checkForItems()
    {
        this.hasWaterskins = false;

        for (int i = 1; i < 10; i++)
        {
            ItemStack stack = this.getItem(i);
            CompoundNBT tag = NBTHelper.getTagOrEmpty(stack);
            double itemTemp = tag.getDouble(FilledWaterskinItem.NBT_TEMPERATURE);

            if (stack.getItem() == ModItems.FILLED_WATERSKIN && itemTemp < 50)
            {   this.hasWaterskins = true;
                break;
            }
        }
    }

    @Override
    public int getSpreadRange()
    {   return ConfigSettings.BOILER_RANGE.get();
    }

    @Override
    public int getMaxRange()
    {   return ConfigSettings.BOILER_MAX_RANGE.get();
    }

    @Override
    public int getMaxPaths()
    {   return ConfigSettings.BOILER_MAX_VOLUME.get();
    }

    @Override
    public int getInsulationTime()
    {   return ConfigSettings.BOILER_WARM_UP_TIME.get();
    }

    @Override
    public int getMaxInsulationLevel()
    {   return ConfigSettings.BOILER_MAX_INSULATION.get();
    }

    @Override
    public SoundEvent getFuelDepleteSound()
    {   return ModSounds.BOILER_DEPLETE;
    }

    @Override
    public boolean hasSmokeStack()
    {   return this.hasSmokestack;
    }

    @Override
    protected void tickPaths(int firstIndex, int lastIndex)
    {
        if (this.hasSmokestack)
        {   super.tickPaths(firstIndex, lastIndex);
        }
    }

    @Override
    protected void tickDrainFuel()
    {
        int fuelInterval = ConfigSettings.BOILER_FUEL_INTERVAL.get();
        if (fuelInterval > 0 && this.ticksExisted % fuelInterval == 0)
        {   this.drainFuel();
        }
    }

    @Override
    public List<Direction> getHeatingSides()
    {   return Arrays.asList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN);
    }

    @Override
    public List<Direction> getCoolingSides()
    {   return Arrays.asList();
    }

    @Override
    public int getItemFuel(ItemStack item)
    {   return ConfigHelper.findFirstFuelMatching(ConfigSettings.BOILER_FUEL, item)
               .map(FuelData::fuel).orElse(0d).intValue();
    }

    @Override
    protected void storeFuel(ItemStack stack, int amount)
    {
        if (this.getFuel() < this.getMaxFuel() - Math.abs(amount) * 0.75)
        {
            if (!stack.hasContainerItem() || stack.getCount() > 1)
            {   int consumeCount = Math.min((int) Math.floor((this.getMaxFuel() - this.getFuel()) / (double) Math.abs(amount)), stack.getCount());
                stack.shrink(consumeCount);
                addFuel(amount * consumeCount);
            }
            else
            {   this.setItem(0, stack.getContainerItem());
                addFuel(amount);
            }
        }
    }

    public int getFuel()
    {   return this.getHotFuel();
    }

    public void setFuel(int amount)
    {   this.setHotFuel(amount, true);
    }

    @Override
    public void setHotFuel(int amount, boolean update)
    {   super.setHotFuel(amount, update);
    }

    @Override
    public void addFuel(int amount)
    {   this.setHotFuelAndUpdate(this.getHotFuel() + amount);
    }

    @Override
    protected boolean isFuelChanged()
    {   return this.ticksExisted % 10 == 0;
    }

    @Override
    protected Container createMenu(int id, PlayerInventory playerInv)
    {   return new BoilerContainer(id, playerInv, this);
    }

    @Override
    protected void tickParticles()
    {
        if (this.hasSmokestack)
        {   super.tickParticles();
        }
    }

    @Override
    public int getContainerSize()
    {   return 10;
    }

    @Override
    public int[] getSlotsForFace(Direction side)
    {   return side.getAxis() == Direction.Axis.Y ? WATERSKIN_SLOTS : FUEL_SLOT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction)
    {
        if (slot == 0)
            return this.getItemFuel(stack) != 0;
        else return ModItemTags.BOILER_VALID.contains(stack.getItem());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction face)
    {
        if (!this.remove && face != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            switch (face)
            {
                case UP : return slotHandlers[0].cast();
                case DOWN : return slotHandlers[1].cast();
                default : return slotHandlers[2].cast();
            }
        }
        return super.getCapability(capability, face);
    }
}
