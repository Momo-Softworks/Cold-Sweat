package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SoulLampInputMessage
{
    public int putSlot;
    public ItemStack lampStack;
    public boolean depositOne;

    public SoulLampInputMessage(int slot, ItemStack stack, boolean depositOne) {
        this.putSlot = slot;
        this.lampStack = stack;
        this.depositOne = depositOne;
    }

    public static void encode(SoulLampInputMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.putSlot);
        buffer.writeItemStack(message.lampStack, true);
        buffer.writeBoolean(message.depositOne);
    }

    public static SoulLampInputMessage decode(FriendlyByteBuf buffer)
    {
        return new SoulLampInputMessage(buffer.readInt(), buffer.readItem(), buffer.readBoolean());
    }

    public static void handle(SoulLampInputMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();

            AbstractContainerMenu container = player.containerMenu;
            ItemStack stack = message.lampStack;
            Slot slot = container.getSlot(message.putSlot);
            ItemStack stack1 = player.isCreative() ? player.getInventory().getItem(message.putSlot) : slot.getItem();
            float fuel = stack1.getOrCreateTag().getFloat("fuel");

            stack1.getOrCreateTag().putFloat("fuel", Math.min(64, fuel + (message.depositOne ? 1 : stack.getCount())));
            stack.shrink(message.depositOne ? 1 : 64 - (int) fuel);

            container.setCarried(stack);

            if (!player.isCreative())
                container.setItem(container.containerId, message.putSlot, stack1);

            ColdSweatPacketHandler.INSTANCE.sendTo(new SoulLampInputClientMessage(stack), player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
        });
        context.setPacketHandled(true);
    }
}
