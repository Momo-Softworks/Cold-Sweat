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
import net.momostudios.coldsweat.config.FuelItemsConfig;
import net.momostudios.coldsweat.core.init.TileEntityInit;
import net.momostudios.coldsweat.core.util.ModItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
        this.ticksExisted++;
        this.ticksExisted %= 1000;

        if (this.ticksExisted % 20 == 0)
        {
            List<BlockPos> positions = this.getPoints();

            if (positions.isEmpty())
            {
                positions.add(pos);
            }

            // Create temporary new list
            List<BlockPos> positions2 = new ArrayList<>();

            for (BlockPos blockPos : positions)
            {
                // Check in all 6 directions
                for (Direction direction : Direction.values())
                {
                    BlockPos testpos = blockPos.add(direction.getDirectionVec());
                    if (!world.getBlockState(testpos).isSolid() && !world.canBlockSeeSky(testpos) && !positions.contains(testpos) && !positions2.contains(testpos))
                    {
                        // Just for testing
                        AxisAlignedBB aabb = new AxisAlignedBB(testpos.getX(), testpos.getY(), testpos.getZ(), testpos.getX() + 1, testpos.getY() + 1, testpos.getZ() + 1);
                        for (PlayerEntity player : world.getEntitiesWithinAABB(PlayerEntity.class, aabb))
                        {
                            player.addPotionEffect(new EffectInstance(Effects.SPEED, 30, 0));
                        }

                        if (Math.sqrt(testpos.distanceSq(this.pos)) <= 30)
                        {
                            positions2.add(testpos);
                        }
                    }
                }
            }
            // Add new positions
            this.addPoints(positions2);
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

        for (INBT point : this.getTileData().getList("points", 3))
        {
            System.out.println("bueh");
            int[] values = ((IntArrayNBT) point).getIntArray();
            pointList.add(new BlockPos(values[0], values[1], values[2]));
        }
        return pointList;
    }

    public void addPoints(List<BlockPos> pointList)
    {
        ListNBT lnbt = this.getTileData().getList("points", 3);
        for (BlockPos bpos : pointList)
        {
            int[] values = {bpos.getX(), bpos.getY(), bpos.getZ()};
            lnbt.add(new IntArrayNBT(values));
        }
        this.getTileData().put("points", lnbt);
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
