package dev.momostudios.coldsweat.common.blockentity;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.block.BoilerBlock;
import dev.momostudios.coldsweat.common.container.BoilerContainer;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.BlockDataUpdateMessage;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.config.LoadedValue;
import dev.momostudios.coldsweat.util.registries.ModBlockEntities;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BoilerBlockEntity extends BaseContainerBlockEntity implements MenuProvider
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};
    public static int SLOTS = 10;
    public static int MAX_FUEL = 1000;
    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    List<ServerPlayer> usingPlayers = new ArrayList<>();
    public int ticksExisted;
    int fuel;

    public static LoadedValue<Map<Item, Number>> VALID_FUEL = LoadedValue.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().boilerItems()));

    public BoilerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.BOILER, pos, state);
    }

    @Nonnull
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

    private void sendUpdatePacket()
    {
        // Remove the players that aren't interacting with this block anymore
        usingPlayers.removeIf(player -> !(player.containerMenu instanceof BoilerContainer boilerContainer && boilerContainer.te == this));

        // Send data to all players with this block's menu open
        ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.NMLIST.with(()-> usingPlayers.stream().map(player -> player.connection.connection).toList()),
                                             new BlockDataUpdateMessage(this));
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container." + ColdSweat.MOD_ID + ".boiler");
    }

    @Override
    public Component getDisplayName() {
        return this.getCustomName() != null ? this.getCustomName() : this.getDefaultName();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T te)
    {
        if (te instanceof BoilerBlockEntity iceboxTE)
        {
            iceboxTE.tick(level, state, pos);
        }
    }

    public void tick(Level level, BlockState state, BlockPos pos)
    {
        ticksExisted++;

        if (getFuel() > 0)
        {
            // Set state to lit
            if (!state.getValue(BoilerBlock.LIT))
                level.setBlock(pos, state.setValue(BoilerBlock.LIT, true), 3);

            // Warm up waterskins
            if (ticksExisted % 20 == 0)
            {
                boolean hasItemStacks = false;
                for (int i = 1; i < 10; i++)
                {
                    ItemStack stack = getItem(i);
                    int itemTemp = stack.getOrCreateTag().getInt("temperature");

                    if (stack.getItem() == ModItems.FILLED_WATERSKIN && itemTemp < 50)
                    {
                        hasItemStacks = true;
                        stack.getOrCreateTag().putInt("temperature", itemTemp + 1);
                    }
                }
                if (hasItemStacks) setFuel(getFuel() - 1);
            }
        }
        // if no fuel, set state to unlit
        else if (state.getValue(BoilerBlock.LIT))
        {
            level.setBlock(pos, state.setValue(BoilerBlock.LIT, false), 3);
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

    public int getItemFuel(ItemStack item)
    {
        return VALID_FUEL.get().getOrDefault(item.getItem(), 0).intValue();
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
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
    {
        // Track the players using this block
        if (playerInv.player instanceof ServerPlayer serverPlayer)
        {
            usingPlayers.add(serverPlayer);
        }
        return new BoilerContainer(id, playerInv, this);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        this.setFuel(tag.getInt("fuel"));
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("fuel", this.getFuel());
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
