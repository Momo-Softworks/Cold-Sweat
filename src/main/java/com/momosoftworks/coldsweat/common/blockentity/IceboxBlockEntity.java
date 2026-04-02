package com.momosoftworks.coldsweat.common.blockentity;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.IceboxBlock;
import com.momosoftworks.coldsweat.common.container.IceboxContainer;
import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.BlockEntityInit;
import com.momosoftworks.coldsweat.core.init.ParticleTypesInit;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.render.ChestLidController;
import com.momosoftworks.coldsweat.util.render.ContainerOpenersCounter;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@OnlyIn(value = Dist.CLIENT, _interface = IChestLid.class)
public class IceboxBlockEntity extends HearthBlockEntity implements ITickableTileEntity, ISidedInventory, IChestLid
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};

    private boolean hasItemStacks = false;
    LazyOptional<? extends IItemHandler>[] slotHandlers =
            SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter()
    {
        protected void onOpen(World level, BlockPos pos, BlockState state)
        {
            if (!IceboxBlockEntity.this.hasSmokestack())
            {   IceboxBlockEntity.this.level.playSound(null, pos, ModSounds.ICEBOX_OPEN, SoundCategory.BLOCKS, 1f, level.random.nextFloat() * 0.2f + 0.9f);
            }
        }

        protected void onClose(World level, BlockPos pos, BlockState state)
        {
            if (!IceboxBlockEntity.this.hasSmokestack())
            {   IceboxBlockEntity.this.level.playSound(null, pos, ModSounds.ICEBOX_CLOSE, SoundCategory.BLOCKS, 1f, 1f);
            }
        }

        protected void openerCountChanged(World level, BlockPos pos, BlockState state, int eventId, int eventParam)
        {   IceboxBlockEntity.this.signalOpenCount(level, pos, state, eventId, eventParam);
        }

        protected boolean isOwnContainer(PlayerEntity player)
        {   return player.containerMenu instanceof IceboxContainer && ((IceboxContainer) player.containerMenu).te.equals(IceboxBlockEntity.this);
        }
    };

    ChestLidController lidController = new ChestLidController();

    public IceboxBlockEntity()
    {   super(BlockEntityInit.ICEBOX_BLOCK_ENTITY_TYPE.get());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {   handleUpdateTag(null, pkt.getTag());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {   return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
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

    protected void signalOpenCount(World level, BlockPos pos, BlockState state, int eventId, int eventParam)
    {   Block block = state.getBlock();
        level.blockEvent(pos, block, eventId, eventParam);
    }

    @Override
    protected ITextComponent getDefaultName()
    {   return new TranslationTextComponent("container." + ColdSweat.MOD_ID + ".icebox");
    }

    @Override
    public void tick()
    {
        super.tick();

        // Tick lid animation on client
        if (level.isClientSide())
        {   this.lidController.tickLid();
        }

        BlockState state = this.getBlockState();

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
                    CompoundNBT tag = NBTHelper.getTagOrEmpty(stack);
                    double itemTemp = tag.getDouble(FilledWaterskinItem.NBT_TEMPERATURE);

                    if (stack.getItem() == ModItems.FILLED_WATERSKIN && itemTemp > -50)
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
    {   return Arrays.asList();
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
    protected Container createMenu(int id, PlayerInventory playerInv)
    {   return new IceboxContainer(id, playerInv, this);
    }

    @Override
    public void startOpen(PlayerEntity player)
    {
        super.startOpen(player);
        this.openersCounter.incrementOpeners(player, this.level, this.getBlockPos(), this.getBlockState());
    }

    @Override
    public void stopOpen(PlayerEntity player)
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
    public BasicParticleType getAirParticle()
    {   return ParticleTypesInit.GROUND_MIST.get();
    }

    @Override
    public void spawnAirParticle(int x, int y, int z, Random rand)
    {
        if (rand.nextFloat() > (spreading ? 0.016f : 0.032f))
        {   return;
        }

        float xr = rand.nextFloat();
        float yr = rand.nextFloat();
        float zr = rand.nextFloat();
        float xm = rand.nextFloat() / 20 - 0.025f;
        float zm = rand.nextFloat() / 20 - 0.025f;

        level.addParticle(ParticleTypesInit.COLD_AIR.get(), false, x + xr, y + yr, z + zr, xm, 0, zm);
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
        else return ModItemTags.ICEBOX_VALID.contains(stack.getItem()) || stack.isEdible();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction face)
    {
        if (!this.remove && face != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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
