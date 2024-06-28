package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UseFilledWaterskinMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<UseFilledWaterskinMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "use_filled_waterskin"));
    public static final StreamCodec<FriendlyByteBuf, UseFilledWaterskinMessage> CODEC = CustomPacketPayload.codec(UseFilledWaterskinMessage::encode, UseFilledWaterskinMessage::decode);

    public UseFilledWaterskinMessage()
    {
    }

    public static void encode(UseFilledWaterskinMessage message, FriendlyByteBuf buffer)
    {
    }

    public static UseFilledWaterskinMessage decode(FriendlyByteBuf buffer)
    {   return new UseFilledWaterskinMessage();
    }

    public static void handle(UseFilledWaterskinMessage message, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof FilledWaterskinItem)
            {   FilledWaterskinItem.performPourAction(stack, player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}