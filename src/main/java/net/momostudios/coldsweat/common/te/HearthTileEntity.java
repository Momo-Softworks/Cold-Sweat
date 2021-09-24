package net.momostudios.coldsweat.common.te;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.block.HearthBlock;
import net.momostudios.coldsweat.common.container.HearthContainer;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.HearthTempModifier;
import net.momostudios.coldsweat.config.FuelItemsConfig;
import net.momostudios.coldsweat.core.init.TileEntityInit;
import net.momostudios.coldsweat.core.util.ModItems;
import net.momostudios.coldsweat.core.util.PlayerTemp;
import net.momostudios.coldsweat.core.util.WorldInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class HearthTileEntity extends LockableLootTileEntity implements ITickableTileEntity
{
    public static int slots = 10;
    protected NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);
    public int ticksExisted;
    protected static int MAX_FUEL = 1000;

    public HearthTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container." + ColdSweat.MOD_ID + ".hearth");
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

    public HearthTileEntity()
    {
        this(TileEntityInit.HEARTH_TILE_ENTITY_TYPE.get());
    }

    public void tick()
    {
        this.ticksExisted = (this.ticksExisted + 1) % 1000;

        List<PlayerEntity> affectedPlayers = new ArrayList<>();

        // Get master list of points (and add a dummy one if it is empty)
        List<BlockPos> poss = this.getPoints();
        if (poss.isEmpty()) {
            poss.add(pos);
            poss.add(pos.up());
        }

        // Create temporary new list
        List<BlockPos> positions2 = new ArrayList<>();

        /*
         Partition all points into multiple lists (max of 19)
        */
        List<List<BlockPos>> partitions = new ArrayList<>();

        // Size of each partition
        int partSize = 400;
        // Number of partitions with 150 elements each
        int partitionCount = Math.max(poss.size() / partSize, poss.size());

        // Iterate through poss and divide into multiple lists
        int index = 0;
        for (int l = 0; l < partitionCount; l++)
        {
            // break if the end is reached
            if (index >= poss.size()) break;

            // Add a new partition to [partitions] and move on to the next set
            partitions.add(poss.subList(index, Math.min(Math.max(0, poss.size() - 1), index + partSize)));
            index += partSize;
        }

        // The index of the partition being worked on (based on current world tick)
        int scanningIndex = this.ticksExisted % partitions.size();

        /*
         Iterate through the partition with index [scanningIndex]
         */
        if (!partitions.isEmpty())
        {
            for (BlockPos blockPos : partitions.get(scanningIndex))
            {
                // Create detection box for PlayerEntities
                AxisAlignedBB aabb = new AxisAlignedBB(blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                    blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1);

                // Apply insulation effect
                affectedPlayers.addAll(world.getEntitiesWithinAABB(PlayerEntity.class, aabb));

                //world.addParticle(ParticleTypes.FLAME, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 0, 0, 0);

                // Check in all 6 directions
                for (Direction direction : Direction.values())
                {
                    // Create new BlockPos with an offset of [direction] from [blockPos]
                    BlockPos testpos = blockPos.add(direction.getDirectionVec());
                    BlockPos testpos2 = blockPos.add(direction.getXOffset() * 2, direction.getYOffset() * 2, direction.getZOffset() * 2);

                    if (!world.getBlockState(testpos).isSolid() && !world.canBlockSeeSky(testpos) && !poss.contains(testpos) && !positions2.contains(testpos)
                    &&  !world.getBlockState(testpos2).isSolid() && !world.canBlockSeeSky(testpos) && !poss.contains(testpos2) && !positions2.contains(testpos2))
                    {
                        // Returns true if [testpos] is adjacent to a solid on any side
                        boolean touchingSolid = WorldInfo.adjacentPositions(testpos).stream().anyMatch(newPos -> world.getBlockState(newPos).isSolid());
                        boolean touchingSolid2 = WorldInfo.adjacentPositions(testpos2).stream().anyMatch(newPos -> world.getBlockState(newPos).isSolid());

                        // Test if [testpos] is within 24 blocks
                        if (Math.sqrt(testpos.distanceSq(this.pos)) <= 24 &&
                        (touchingSolid || (testpos.getX() % 2 == 0 && testpos.getY() % 2 == 0 && testpos.getZ() % 2 == 0)))
                        {
                            positions2.add(testpos);
                        }
                        if (Math.sqrt(testpos2.distanceSq(this.pos)) <= 24 &&
                            (touchingSolid2 || (testpos2.getX() % 2 == 0 && testpos2.getY() % 2 == 0 && testpos2.getZ() % 2 == 0)))
                        {
                            positions2.add(testpos2);
                        }
                    }
                }
            }
            // Add new positions to NBT
            this.addPoints(positions2);
        }

        // Periodically reset points
        if (this.ticksExisted % 400 == 0)
        {
            this.clearPoints();
        }

        if (!getFuelItem(this.getItemInSlot(0)).isEmpty())
        {
            ItemStack fuel = this.getItemInSlot(0);
            int amount = (int) getFuelItem(this.getItemInSlot(0)).get(1);
            if (this.getHotFuel() <= Math.max(MAX_FUEL - amount, MAX_FUEL * 0.9) && amount > 0)
            {
                if (fuel.hasContainerItem() && fuel.getCount() == 1)
                {
                    this.setItemInSlot(0, fuel.getContainerItem());
                }
                else this.getItemInSlot(0).shrink(1);
                this.setHotFuel(this.getHotFuel() + amount);
            }

            if (this.getColdFuel() <= Math.max(MAX_FUEL - amount, MAX_FUEL * 0.9) && amount < 0)
            {
                if (fuel.hasContainerItem() && fuel.getCount() == 1)
                {
                    this.setItemInSlot(0, fuel.getContainerItem());
                }
                else this.getItemInSlot(0).shrink(1);
                this.setColdFuel(this.getColdFuel() - amount);
            }
        }
    }

    public List getFuelItem(ItemStack item)
    {
        List returnList = new ArrayList();
        for (Object iterator : FuelItemsConfig.hearthItems.get())
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

    public int getHotFuel()
    {
        return this.getTileData().getInt("hot_fuel");
    }

    public int getColdFuel()
    {
        return this.getTileData().getInt("cold_fuel");
    }

    public void setHotFuel(int amount)
    {
        this.getTileData().putInt("hot_fuel", Math.min(amount, MAX_FUEL));
    }

    public void setColdFuel(int amount)
    {
        this.getTileData().putInt("cold_fuel", Math.min(amount, MAX_FUEL));
    }

    public List<BlockPos> getPoints()
    {
        List<BlockPos> pointList = new ArrayList<>();

        for (INBT point : this.getTileData().getList("points", 11))
        {
            int[] values = ((IntArrayNBT) point).getIntArray();
            pointList.add(new BlockPos(values[0], values[1], values[2]));
        }
        return pointList;
    }

    public void addPoints(List<BlockPos> pointList)
    {
        ListNBT points = this.getTileData().getList("points", 11);
        for (BlockPos bpos : pointList)
        {
            int[] values = {bpos.getX(), bpos.getY(), bpos.getZ()};
            points.add(new IntArrayNBT(values));
        }
        this.getTileData().put("points", points);
    }

    public void clearPoints()
    {
        this.getTileData().put("points", new ListNBT());
    }

    @Override
    public int getSizeInventory() {
        return slots;
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player)
    {
        return new HearthContainer(id, player, this);
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
