package com.momosoftworks.coldsweat.common.tileentity;

import com.momosoftworks.coldsweat.common.block.BoilerBlock;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.ItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoilerTileEntity extends TileEntity implements ISidedInventory, ITileEntityDataHolder
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};
    public static int SLOTS = 10;
    public static int MAX_FUEL = 1000;

    private ItemStack[] inventory = new ItemStack[SLOTS];
    private String name;
    NBTTagCompound tileData = new NBTTagCompound();
    public long ticksExisted;
    int fuel;
    List<EntityPlayerMP> usingPlayers = new ArrayList<>();

    @Override
    public void updateEntity()
    {
        ticksExisted++;

        if (getFuel() > 0)
        {
            // Set state to lit
            if (this.blockType != ModBlocks.BOILER_LIT)
            {   BoilerBlock.updateBlockState(true, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            }

            // Warm up waterskins
            if (ticksExisted % 20 == 0)
            {
                boolean hasItemStacks = false;
                for (int i = 1; i < 10; i++)
                {
                    ItemStack stack = this.getStackInSlot(i);
                    if (stack == null) continue;
                    int itemTemp = ItemHelper.getOrCrateTag(stack).getInteger("temperature");

                    if (stack.getItem() == ModItems.FILLED_WATERSKIN && itemTemp < 50)
                    {   hasItemStacks = true;
                        ItemHelper.getOrCrateTag(stack).setInteger("temperature", itemTemp + 1);
                    }
                }
                if (hasItemStacks) this.setFuel(getFuel() - 1);
            }
        }
        // If no fuel, set state to unlit
        else if (this.blockType == ModBlocks.BOILER_LIT)
        {   BoilerBlock.updateBlockState(false, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        }

        // Input fuel
        if (this.ticksExisted % 10 == 0)
        {
            ItemStack fuelStack = this.getStackInSlot(0);
            int itemFuel = getItemFuel(fuelStack);

            if (itemFuel != 0 && this.getFuel() < MAX_FUEL - itemFuel / 2)
            {
                if (ItemHelper.hasContainerItem(fuelStack) && fuelStack.stackSize == 1)
                {   this.setInventorySlotContents(0, ItemHelper.getContainerItem(fuelStack));
                    this.setFuel(this.getFuel() + itemFuel);
                }
                else
                {   int consumeCount = Math.min((int) Math.floor((MAX_FUEL - fuel) / (double) Math.abs(itemFuel)), fuelStack.stackSize);
                    this.setInventorySlotContents(0, ItemHelper.grow(fuelStack, -consumeCount));
                    this.setFuel(this.getFuel() + itemFuel * consumeCount);
                }
            }
        }
    }

    public int getItemFuel(ItemStack item)
    {   if (item == null) return 0;
        return ConfigSettings.BOILER_FUEL.get().getOrDefault(item.getItem(), 0d).intValue();
    }

    public int getFuel()
    {   return fuel;
    }

    public void setFuel(int amount)
    {
        fuel = Math.min(amount, MAX_FUEL);
        if (this.worldObj != null && !this.worldObj.isRemote)
        {   //this.sendUpdatePacket();
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int direction)
    {   ForgeDirection direc = ForgeDirection.getOrientation(direction);
        return direc == ForgeDirection.DOWN || direc == ForgeDirection.UP ? WATERSKIN_SLOTS : FUEL_SLOT;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side)
    {   return Arrays.stream(getAccessibleSlotsFromSide(side)).anyMatch(i -> i == slot);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side)
    {   return this.canInsertItem(slot, stack, side) && slot != 0;
    }

    @Override
    public int getSizeInventory()
    {   return SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {   return this.inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int by)
    {   return inventory[slot] = ItemHelper.grow(this.getStackInSlot(slot), -by);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot)
    {   ItemStack stack = this.getStackInSlot(slot);
        this.setInventorySlotContents(slot, null);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        if (stack != null)
        {   stack.stackSize = Math.min(stack.stackSize, this.getInventoryStackLimit());
            this.inventory[slot] = stack;
        }
    }

    @Override
    public String getInventoryName()
    {   return this.hasCustomInventoryName() ? this.name : new ChatComponentTranslation("container.cold_sweat.boiler").getUnformattedTextForChat();
    }

    @Override
    public boolean hasCustomInventoryName()
    {   return this.name != null && !this.name.isEmpty();
    }

    public void setName(String name)
    {   this.name = name;
    }

    @Override
    public int getInventoryStackLimit()
    {   return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {   return player.getDistanceSq(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5) <= 64
            && player.dimension == this.worldObj.provider.dimensionId;
    }

    @Override
    public void openInventory()
    {

    }

    @Override
    public void closeInventory()
    {

    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {   return slot == 0 ? getItemFuel(stack) > 0 : stack.getItem() == ModItems.FILLED_WATERSKIN;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.tileData = nbt.getCompoundTag("TileData");
        // Read inventory
        NBTTagList itemNBTList = nbt.getTagList("Items", 10);
        this.inventory = new ItemStack[this.getSizeInventory()];
        for (int i = 0; i < itemNBTList.tagCount(); i++)
        {   NBTTagCompound itemNBT = itemNBTList.getCompoundTagAt(i);
            this.setInventorySlotContents(itemNBT.getByte("Slot"), ItemStack.loadItemStackFromNBT(itemNBT));
        }
        //Read fuel
        this.setFuel(nbt.getInteger("Fuel"));
        // Read Name
        if (nbt.hasKey("CustomName", 8))
        {   this.name = nbt.getString("CustomName");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setTag("TileData", this.tileData);
        // Write inventory
        NBTTagList itemNBTList = new NBTTagList();
        for (int i = 0; i < this.getSizeInventory(); i++)
        {   ItemStack stack = this.getStackInSlot(i);
            if (stack != null)
            {   NBTTagCompound itemNBT = new NBTTagCompound();
                itemNBT.setByte("Slot", (byte) i);
                stack.writeToNBT(itemNBT);
                itemNBTList.appendTag(itemNBT);
            }
        }
        nbt.setTag("Items", itemNBTList);
        // Write fuel
        nbt.setInteger("Fuel", this.getFuel());
        // Write name
        if (this.hasCustomInventoryName())
        {   nbt.setString("CustomName", this.name);
        }
    }

    @Override
    public NBTTagCompound getTileData()
    {   return tileData;
    }
}
