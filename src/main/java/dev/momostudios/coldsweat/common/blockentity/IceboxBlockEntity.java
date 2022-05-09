package dev.momostudios.coldsweat.common.blockentity;

import dev.momostudios.coldsweat.common.block.IceboxBlock;
import dev.momostudios.coldsweat.core.init.ParticleTypesInit;
import dev.momostudios.coldsweat.util.registries.ModBlockEntities;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.container.IceboxContainer;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;

import java.util.List;
import java.util.Random;

public class IceboxBlockEntity extends BaseContainerBlockEntity implements MenuProvider
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};
    public static int slots = 10;
    public static int MAX_FUEL = 1000;
    protected NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);
    public int ticksExisted;
    private int fuel;

    public IceboxBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.get("icebox"), pos, state);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("fuel", this.getFuel());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        super.handleUpdateTag(tag);
        this.setFuel(tag.getInt("fuel"));
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container." + ColdSweat.MOD_ID + ".icebox");
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T te)
    {
        if (te instanceof IceboxBlockEntity iceboxTE)
        {
            iceboxTE.tick(level, state, pos);
        }
    }

    public void tick(Level level, BlockState state, BlockPos pos)
    {
        ticksExisted++;
        ticksExisted %= 1000;

        if (getFuel() > 0)
        {
            // Set state to frosted
            if (!state.getValue(IceboxBlock.FROSTED))
                level.setBlock(pos, state.setValue(IceboxBlock.FROSTED, true), 3);

            // Cool down waterskins
            if (ticksExisted % 20 == 0)
            {
                boolean hasItemStacks = false;
                for (int i = 0; i < 10; i++)
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
        else
        {
            if (state.getValue(IceboxBlock.FROSTED))
                level.setBlock(pos, state.setValue(IceboxBlock.FROSTED, false), 3);
        }

        // Input fuel
        if (this.ticksExisted % 10 == 0)
        {
            ItemStack fuelStack = this.getItem(0);
            int itemFuel = getItemFuel(fuelStack);
            if (itemFuel != 0)
            {
                if (fuelStack.hasContainerItem())
                {
                    if (fuelStack.getCount() == 1)
                    {
                        this.setItem(0, fuelStack.getContainerItem());
                        setFuel(fuel + itemFuel);
                    }
                }
                else
                {
                    int consumeCount = (int) Math.floor((double) (MAX_FUEL - fuel) / itemFuel);
                    fuelStack.shrink(consumeCount);
                    setFuel(fuel + itemFuel * consumeCount);
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
        int fuel = 0;
        for (List<?> testIndex : new ItemSettingsConfig().iceboxItems())
        {
            String testItem = (String) testIndex.get(0);

            if (new ResourceLocation(testItem).equals(ForgeRegistries.ITEMS.getKey(item.getItem())))
            {
                fuel = ((Number) testIndex.get(1)).intValue();
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
        return fuel;
    }

    public void setFuel(int amount)
    {
        fuel = Math.min(amount, MAX_FUEL);
    }

    public LazyOptional<IItemHandler> getCap()
    {
        return this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
    {
        return new IceboxContainer(id, playerInv, this);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.setFuel(tag.getInt("fuel"));
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("fuel", this.getFuel());
        ContainerHelper.saveAllItems(tag, this.items);
    }

    @Override
    public int getContainerSize()
    {
        return slots;
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
    public ItemStack removeItem(int slot, int count)
    {
        ItemStack itemstack = ContainerHelper.removeItem(items, slot, count);

        if (!itemstack.isEmpty())
        {
            this.setChanged();
        }
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        return ContainerHelper.removeItem(items, slot, items.get(slot).getCount());
    }

    @Override
    public void setItem(int slot, ItemStack itemstack)
    {
        items.set(slot, itemstack);
    }

    @Override
    public boolean stillValid(Player player)
    {
        return player.distanceToSqr(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent()
    {
        items.clear();
    }
}
