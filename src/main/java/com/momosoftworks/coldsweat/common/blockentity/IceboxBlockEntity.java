package com.momosoftworks.coldsweat.common.blockentity;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.IceboxBlock;
import com.momosoftworks.coldsweat.common.container.IceboxContainer;
import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ParticleTypesInit;
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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class IceboxBlockEntity extends HearthBlockEntity implements LidBlockEntity
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};

    private boolean hasItemStacks = false;
    LazyOptional<? extends IItemHandler>[] slotHandlers =
            SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter()
    {
        protected void onOpen(Level level, BlockPos pos, BlockState state)
        {
            if (!IceboxBlockEntity.this.hasSmokestack())
            {   IceboxBlockEntity.this.level.playSound(null, pos, ModSounds.ICEBOX_OPEN, SoundSource.BLOCKS, 1f, level.random.nextFloat() * 0.2f + 0.9f);
            }
        }

        protected void onClose(Level level, BlockPos pos, BlockState state)
        {
            if (!IceboxBlockEntity.this.hasSmokestack())
            {   IceboxBlockEntity.this.level.playSound(null, pos, ModSounds.ICEBOX_CLOSE, SoundSource.BLOCKS, 1f, 1f);
            }
        }

        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int eventId, int eventParam)
        {   IceboxBlockEntity.this.signalOpenCount(level, pos, state, eventId, eventParam);
        }

        protected boolean isOwnContainer(Player player)
        {   return player.containerMenu instanceof IceboxContainer container && container.te.equals(IceboxBlockEntity.this);
        }
    };

    ChestLidController lidController = new ChestLidController();

    public IceboxBlockEntity(BlockPos pos, BlockState state)
    {   super(ModBlockEntities.ICEBOX, pos, state);
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
    public boolean triggerEvent(int id, int type)
    {
        if (id == 1)
        {   this.lidController.shouldBeOpen(type > 0);
            return true;
        }
        else
        {   return super.triggerEvent(id, type);
        }
    }

    protected void signalOpenCount(Level level, BlockPos pos, BlockState state, int eventId, int eventParam)
    {   Block block = state.getBlock();
        level.blockEvent(pos, block, eventId, eventParam);
    }

    @Override
    protected Component getDefaultName()
    {   return Component.translatable("container." + ColdSweat.MOD_ID + ".icebox");
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T te)
    {
        if (te instanceof IceboxBlockEntity iceboxTE)
        {
            iceboxTE.tick(level, state, pos);
            // Tick lid animation on client
            if (level.isClientSide())
            {   tickAnimateLid(level, pos, state, iceboxTE);
            }
        }
    }

    public static <T extends BlockEntity> void tickAnimateLid(Level level, BlockPos pos, BlockState state, T blockEntity)
    {
        if (blockEntity instanceof IceboxBlockEntity icebox)
        {   icebox.lidController.tickLid();
        }
    }

    public void tick(Level level, BlockState state, BlockPos pos)
    {
        super.tick(level, pos);

        // Recheck openers
        if (!this.remove && this.level != null && this.level.getServer() != null)
        {   this.openersCounter.recheckOpeners(this.level, this.getBlockPos(), this.getBlockState());
        }

        if (!level.isClientSide() && this.getFuel() > 0)
        {
            // Cool down waterskins
            if (ticksExisted % (int) (20 / Math.max(1, ConfigSettings.TEMP_RATE.get())) == 0)
            {
                this.hasItemStacks = false;
                for (int i = 1; i < 10; i++)
                {
                    ItemStack stack = getItem(i);
                    CompoundTag tag = NBTHelper.getTagOrEmpty(stack);
                    double itemTemp = tag.getDouble(FilledWaterskinItem.NBT_TEMPERATURE);

                    if (stack.is(ModItems.FILLED_WATERSKIN) && itemTemp > -50)
                    {   this.hasItemStacks = true;
                        tag.putDouble(FilledWaterskinItem.NBT_TEMPERATURE, Math.max(-50, itemTemp - 1));
                    }
                }
            }
        }
    }

    @Override
    public int getSpreadRange()
    {   return ConfigSettings.ICEBOX_RANGE.get();
    }

    @Override
    public int getMaxRange()
    {   return ConfigSettings.ICEBOX_MAX_RANGE.get();
    }

    @Override
    public int getMaxPaths()
    {   return ConfigSettings.ICEBOX_MAX_VOLUME.get();
    }

    @Override
    public int getInsulationTime()
    {   return ConfigSettings.ICEBOX_WARM_UP_TIME.get();
    }

    @Override
    public int getMaxInsulationLevel()
    {   return ConfigSettings.ICEBOX_MAX_INSULATION.get();
    }

    @Override
    public SoundEvent getFuelDepleteSound()
    {   return ModSounds.ICEBOX_DEPLETE;
    }

    @Override
    public boolean hasSmokestack()
    {   return this.hasSmokestack;
    }

    @Override
    public boolean isSmartEnabled()
    {   return ConfigSettings.SMART_ICEBOX.get();
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
    {   return ConfigSettings.ICEBOX_FUEL_INTERVAL.get();
    }

    @Override
    public boolean isUsingColdFuel()
    {   return super.isUsingColdFuel() || this.hasItemStacks;
    }

    @Override
    public void checkForStateChange()
    {
        super.checkForStateChange();
        this.ensureState(IceboxBlock.FROSTED, this.getColdFuel() > 0);
    }

    @Override
    public List<Direction> getCoolingSides()
    {   return Arrays.asList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN);
    }

    @Override
    public List<Direction> getHeatingSides()
    {   return List.of();
    }

    @Override
    public boolean supportsHeating()
    {   return false;
    }

    @Override
    public int getItemFuel(ItemStack item)
    {   return CSMath.getIfNotNull(ConfigHelper.getFirstOrNull(ConfigSettings.ICEBOX_FUEL, item.getItem(), data -> data.test(item)), FuelData::fuel, 0d).intValue();
    }

    @Override
    protected void storeFuel(ItemStack stack, int amount)
    {
        if (this.getFuel() < this.getMaxFuel() - Math.abs(amount) * 0.75)
        {
            if (!stack.hasCraftingRemainingItem() || stack.getCount() > 1)
            {   int consumeCount = Math.min((int) Math.floor((this.getMaxFuel() - this.getFuel()) / (double) Math.abs(amount)), stack.getCount());
                stack.shrink(consumeCount);
                addFuel(amount * consumeCount);
            }
            else
            {   this.setItem(0, stack.getCraftingRemainingItem());
                addFuel(amount);
            }
        }
    }

    public int getFuel()
    {   return this.getColdFuel();
    }

    public void setFuel(int amount)
    {   this.setColdFuel(amount, true);
    }

    @Override
    public void addFuel(int amount)
    {   this.addColdFuel(amount, true);
    }

    @Override
    protected boolean isFuelChanged()
    {   return this.ticksExisted % 10 == 0;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
    {   return new IceboxContainer(id, playerInv, this);
    }

    @Override
    public void startOpen(Player player)
    {
        super.startOpen(player);
        this.openersCounter.incrementOpeners(player, this.level, this.getBlockPos(), this.getBlockState());
    }

    @Override
    public void stopOpen(Player player)
    {
        super.stopOpen(player);
        this.openersCounter.decrementOpeners(player, this.level, this.getBlockPos(), this.getBlockState());
    }

    @Override
    protected void tickParticles()
    {
        if (this.hasSmokestack)
        {   super.tickParticles();
        }
    }

    @Override
    public ParticleOptions getAirParticle()
    {   return ParticleTypesInit.GROUND_MIST.get();
    }

    @Override
    public void spawnAirParticle(int x, int y, int z, RandomSource rand)
    {
        if (rand.nextFloat() > (spreading ? 0.016f : 0.032f))
        {   return;
        }

        float xr = rand.nextFloat();
        float yr = rand.nextFloat();
        float zr = rand.nextFloat();
        float xm = rand.nextFloat() / 20 - 0.025f;
        float zm = rand.nextFloat() / 20 - 0.025f;

        level.addParticle(ParticleTypesInit.MIST.get(), false, x + xr, y + yr, z + zr, xm, 0, zm);
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
        else return stack.is(ModItemTags.ICEBOX_VALID) || stack.isEdible();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction face)
    {
        if (!this.remove && face != null && capability == ForgeCapabilities.ITEM_HANDLER)
        {
            if (face == Direction.UP)
                return slotHandlers[0].cast();
            else if (face == Direction.DOWN)
                return slotHandlers[1].cast();
            else
                return slotHandlers[2].cast();
        }
        return super.getCapability(capability, face);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction)
    {   return slot > 0;
    }

    @Override
    public float getOpenNess(float partialTick)
    {   return this.lidController.getOpenness(partialTick);
    }
}
