package com.momosoftworks.coldsweat.core.network.message;

import com.mojang.serialization.DynamicOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncContainerSlotMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SyncContainerSlotMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "sync_container_slot"));
    public static final StreamCodec<FriendlyByteBuf, SyncContainerSlotMessage> CODEC = CustomPacketPayload.codec(SyncContainerSlotMessage::encode, SyncContainerSlotMessage::decode);

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
        buffer.writeNbt(msg.stack.save(RegistryHelper.getRegistryAccess()));
        buffer.writeVarInt(msg.containerId);
    }

    public static SyncContainerSlotMessage decode(FriendlyByteBuf buffer)
    {
        int slot = buffer.readInt();
        ItemStack stack = ItemStack.CODEC.decode(NbtOps.INSTANCE, buffer.readNbt()).result().orElseThrow().getFirst();
        int containerId = buffer.readVarInt();
        return new SyncContainerSlotMessage(slot, stack, containerId);
    }

    public static void handle(SyncContainerSlotMessage message, IPayloadContext context)
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

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}