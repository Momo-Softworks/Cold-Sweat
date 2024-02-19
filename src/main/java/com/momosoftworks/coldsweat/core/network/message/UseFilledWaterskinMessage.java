package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UseFilledWaterskinMessage
{
    public UseFilledWaterskinMessage()
    {
    }

    public static void encode(UseFilledWaterskinMessage message, PacketBuffer buffer)
    {
    }

    public static UseFilledWaterskinMessage decode(PacketBuffer buffer)
    {   return new UseFilledWaterskinMessage();
    }

    public static void handle(UseFilledWaterskinMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() -> {
            PlayerEntity player = contextSupplier.get().getSender();
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
