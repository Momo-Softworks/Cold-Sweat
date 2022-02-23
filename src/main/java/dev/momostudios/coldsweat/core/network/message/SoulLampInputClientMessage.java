package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SoulLampInputClientMessage
{
    public ItemStack holdingStack;

    public SoulLampInputClientMessage(ItemStack stack){
        this.holdingStack = stack;
    }

    public static void encode(SoulLampInputClientMessage message, PacketBuffer buffer)
    {
        buffer.writeItemStack(message.holdingStack);
    }

    public static SoulLampInputClientMessage decode(PacketBuffer buffer)
    {
        return new SoulLampInputClientMessage(buffer.readItemStack());
    }

    public static void handle(SoulLampInputClientMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                if (Minecraft.getInstance().player != null)
                {
                    ClientPlayerEntity player = Minecraft.getInstance().player;

                    player.inventory.setItemStack(message.holdingStack);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
