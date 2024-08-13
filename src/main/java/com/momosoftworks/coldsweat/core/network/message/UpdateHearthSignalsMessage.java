package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UpdateHearthSignalsMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<UpdateHearthSignalsMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "update_hearth_signals"));
    public static final StreamCodec<FriendlyByteBuf, UpdateHearthSignalsMessage> CODEC = CustomPacketPayload.codec(UpdateHearthSignalsMessage::encode, UpdateHearthSignalsMessage::decode);

    private final boolean isSidePowered;
    private final boolean isBackPowered;
    private final BlockPos pos;

    public UpdateHearthSignalsMessage(boolean isSidePowered, boolean isBackPowered, BlockPos pos)
    {
        this.isSidePowered = isSidePowered;
        this.isBackPowered = isBackPowered;
        this.pos = pos;
    }

    public static void encode(UpdateHearthSignalsMessage message, FriendlyByteBuf buf)
    {
        buf.writeBoolean(message.isSidePowered);
        buf.writeBoolean(message.isBackPowered);
        buf.writeBlockPos(message.pos);
    }

    public static UpdateHearthSignalsMessage decode(FriendlyByteBuf buf)
    {
        return new UpdateHearthSignalsMessage(buf.readBoolean(), buf.readBoolean(), buf.readBlockPos());
    }

    public static void handle(UpdateHearthSignalsMessage message, IPayloadContext context)
    {
        if (context.flow().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                if (Minecraft.getInstance().level.getBlockEntity(message.pos) instanceof HearthBlockEntity hearth)
                {
                    hearth.setSidePowered(message.isSidePowered);
                    hearth.setBackPowered(message.isBackPowered);
                }
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}
