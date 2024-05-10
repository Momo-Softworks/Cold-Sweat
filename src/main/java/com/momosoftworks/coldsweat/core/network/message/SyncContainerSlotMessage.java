package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncContainerSlotMessage
{
    int slot;
    ItemStack stack;
    int containerId = -1;

    public SyncContainerSlotMessage(int slot, ItemStack stack, int containerId)
    {   this.slot = slot;
        this.stack = stack;
        this.containerId = containerId;
    }

    public SyncContainerSlotMessage(int slot, ItemStack stack, AbstractContainerMenu container)
    {   this.slot = slot;
        this.stack = stack;
        this.containerId = container.containerId;
    }

    public static void encode(SyncContainerSlotMessage msg, FriendlyByteBuf buffer)
    {
        buffer.writeInt(msg.slot);
        buffer.writeItemStack(ItemStack.of(msg.stack.save(new CompoundTag())), false);
        buffer.writeVarInt(msg.containerId);
    }

    public static SyncContainerSlotMessage decode(FriendlyByteBuf buffer)
    {
        int slot = buffer.readInt();
        ItemStack stack = buffer.readItem();
        int containerId = buffer.readVarInt();
        return new SyncContainerSlotMessage(slot, stack, containerId);
    }

    public static void handle(SyncContainerSlotMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                AbstractContainerMenu container = ClientOnlyHelper.getClientPlayer().containerMenu;

                if (container.containerId == message.containerId
                && container.isValidSlotIndex(message.slot))
                {   container.slots.get(message.slot).set(message.stack);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
