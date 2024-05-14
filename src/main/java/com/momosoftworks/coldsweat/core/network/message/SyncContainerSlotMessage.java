package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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

    public SyncContainerSlotMessage(int slot, ItemStack stack, Container container)
    {   this.slot = slot;
        this.stack = stack;
        this.containerId = container.containerId;
    }

    public static void encode(SyncContainerSlotMessage msg, PacketBuffer buffer)
    {
        buffer.writeInt(msg.slot);
        buffer.writeNbt(msg.stack.save(new CompoundNBT()));
        buffer.writeVarInt(msg.containerId);
    }

    public static SyncContainerSlotMessage decode(PacketBuffer buffer)
    {
        int slot = buffer.readInt();
        ItemStack stack = ItemStack.of(buffer.readNbt());
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
                Container container = ClientOnlyHelper.getClientPlayer().containerMenu;

                if (container.containerId == message.containerId
                && CSMath.betweenInclusive(message.slot, 0, container.slots.size() - 1))
                {   container.slots.get(message.slot).set(message.stack);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
