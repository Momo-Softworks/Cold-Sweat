package com.momosoftworks.coldsweat.common.blockentity;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.BoilerBlock;
import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlockEntities;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Arrays;
import java.util.List;

public class BoilerBlockEntity extends HearthBlockEntity
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};

    protected boolean hasWaterskins = false;
    protected boolean hasDrinkables = false;

    LazyOptional<? extends IItemHandler>[] slotHandlers =
            SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    public BoilerBlockEntity(BlockPos pos, BlockState state)
    {   super(ModBlockEntities.BOILER, pos, state);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {   handleUpdateTag(pkt.getTag());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {   return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected Component getDefaultName()
    {   return new TranslatableComponent("container." + ColdSweat.MOD_ID + ".boiler");
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T te)
    {
        if (te instanceof BoilerBlockEntity boilerTE)
        {   boilerTE.tick(level, state, pos);
        }
    }

    public void tick(Level level, BlockState state, BlockPos pos)
    {
        super.tick(level, pos);

        if (!level.isClientSide() && this.getFuel() > 0)
        {
            if (this.ticksExisted % (int) (20 / Math.max(1, ConfigSettings.TEMP_RATE.get())) == 0)
            {
                // Warm up waterskins
                this.hasWaterskins = false;
                for (int i = 1; i < 10; i++)
                {
                    ItemStack stack = this.getItem(i);
                    CompoundTag tag = NBTHelper.getTagOrEmpty(stack);
                    double itemTemp = tag.getDouble(FilledWaterskinItem.NBT_TEMPERATURE);

                    if (stack.is(ModItems.FILLED_WATERSKIN) && itemTemp < 50)
                    {   tag.putDouble(FilledWaterskinItem.NBT_TEMPERATURE, Math.min(50, itemTemp + 1));
                        hasWaterskins = true;
                    }
                }
            }
            // Purify drinkable items
            if (this.ticksExisted % (200 / Math.max(1, ConfigSettings.TEMP_RATE.get())) == 0)
            {
                this.hasDrinkables = false;
                for (int i = 1; i < 10; i++)
                {
                    ItemStack stack = this.getItem(i);
                    if (CompatManager.isThirstLoaded() && CompatManager.Thirst.hasPurity(stack)
                    && CompatManager.Thirst.getPurity(stack) < 3)
                    {
                        CompatManager.Thirst.setPurity(stack, CompatManager.Thirst.getPurity(stack) + 1);
                        hasDrinkables = true;
                    }
                }
            }
        }
    }

    public void checkForItems()
    {
        this.hasWaterskins = false;
        this.hasDrinkables = false;

        for (int i = 1; i < 10; i++)
        {
            ItemStack stack = this.getItem(i);
            CompoundTag tag = NBTHelper.getTagOrEmpty(stack);
            double itemTemp = tag.getDouble(FilledWaterskinItem.NBT_TEMPERATURE);

            if (stack.is(ModItems.FILLED_WATERSKIN) && itemTemp < 50)
            {   this.hasWaterskins = true;
            }
            else if (CompatManager.isThirstLoaded() && CompatManager.Thirst.hasPurity(stack)
            && CompatManager.Thirst.getPurity(stack) < 3)
            {   this.hasDrinkables = true;
            }
        }
    }

    @Override
    protected void init()
    {
        super.init();
        this.checkForItems();
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
    public boolean hasSmokestack()
    {   return this.hasSmokestack;
    }

    @Override
    public boolean isSmartEnabled()
    {   return ConfigSettings.SMART_BOILER.get();
    }

    @Override
    protected void tickPaths(int firstIndex, int lastIndex)
    {
        if (this.hasSmokestack)
        {   super.tickPaths(firstIndex, lastIndex);
        }
    }

    @Override
    public int getFuelDrainInterval()
    {   return ConfigSettings.BOILER_FUEL_INTERVAL.get();
    }

    @Override
    public boolean isUsingHotFuel()
    {   return super.isUsingHotFuel() || this.hasDrinkables || this.hasWaterskins;
    }

    @Override
    public void checkForStateChange()
    {
        super.checkForStateChange();
        this.ensureState(BoilerBlock.LIT, this.isUsingHotFuel());
    }

    @Override
    public List<Direction> getHeatingSides()
    {   return Arrays.asList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    }

    @Override
    public List<Direction> getCoolingSides()
    {   return List.of();
    }

    @Override
    public boolean supportsCooling()
    {   return false;
    }

    @Override
    public int getItemFuel(ItemStack item)
    {
        FuelData fuelData = ConfigHelper.getFirstOrNull(ConfigSettings.BOILER_FUEL, item.getItem(), data -> data.test(item));
        return CSMath.getIfNotNull(fuelData, data -> data.fuel(item), 0);
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
    public void addFuel(int amount)
    {   this.addHotFuel(amount, true);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
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
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction)
    {
        if (slot == 0)
            return this.getItemFuel(stack) != 0;
        else return stack.is(ModItemTags.BOILER_VALID) || (CompatManager.isThirstLoaded() && CompatManager.Thirst.hasPurity(stack));
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction)
    {   return slot > 0;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction face)
    {
        if (!this.remove && face != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return switch (face)
            {
                case UP -> slotHandlers[0].cast();
                case DOWN -> slotHandlers[1].cast();
                default -> slotHandlers[2].cast();
            };
        }
        return super.getCapability(capability, face);
    }
}
