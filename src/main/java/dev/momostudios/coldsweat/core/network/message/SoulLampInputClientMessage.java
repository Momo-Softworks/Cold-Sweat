package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SoulLampInputClientMessage
{
    public ItemStack holdingStack;

    public SoulLampInputClientMessage(ItemStack stack){
        this.holdingStack = stack;
    }

    public static void encode(SoulLampInputClientMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeItemStack(message.holdingStack, true);
    }

    public static SoulLampInputClientMessage decode(FriendlyByteBuf buffer)
    {
        return new SoulLampInputClientMessage(buffer.readItem());
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
                    LocalPlayer player = Minecraft.getInstance().player;

                    player.containerMenu.setCarried(message.holdingStack);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
