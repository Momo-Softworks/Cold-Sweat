package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseFilledWaterskinMessage
{
    public UseFilledWaterskinMessage()
    {
    }

    public static void encode(UseFilledWaterskinMessage message, FriendlyByteBuf buffer)
    {
    }

    public static UseFilledWaterskinMessage decode(FriendlyByteBuf buffer)
    {   return new UseFilledWaterskinMessage();
    }

    public static void handle(UseFilledWaterskinMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() -> {
            Player player = contextSupplier.get().getSender();
            if (player != null)
            {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof FilledWaterskinItem)
                {   FilledWaterskinItem.performPourAction(stack, player);
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
