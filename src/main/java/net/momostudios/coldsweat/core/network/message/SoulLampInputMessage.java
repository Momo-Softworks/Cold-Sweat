package net.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;

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

    public static void encode(SoulLampInputMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.putSlot);
        buffer.writeItemStack(message.lampStack);
        buffer.writeBoolean(message.depositOne);
    }

    public static SoulLampInputMessage decode(PacketBuffer buffer)
    {
        return new SoulLampInputMessage(buffer.readInt(), buffer.readItemStack(), buffer.readBoolean());
    }

    public static void handle(SoulLampInputMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            ServerPlayerEntity player = context.getSender();

            Container container = player.openContainer;
            ItemStack stack = message.lampStack;
            Slot slot = container.getSlot(message.putSlot);
            ItemStack stack1 = Minecraft.getInstance().currentScreen instanceof CreativeScreen ? player.inventory.getStackInSlot(message.putSlot) :slot.getStack();
            float fuel = stack1.getOrCreateTag().getFloat("fuel");

            stack1.getOrCreateTag().putFloat("fuel", Math.min(64, fuel + (message.depositOne ? 1 : stack.getCount())));
            stack.shrink(message.depositOne ? 1 : 64 - (int) fuel);

            player.inventory.setItemStack(stack);

            if (!player.isCreative())
                container.putStackInSlot(message.putSlot, stack1);

            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SoulLampInputClientMessage(stack));
        });
        context.setPacketHandled(true);
    }
}
