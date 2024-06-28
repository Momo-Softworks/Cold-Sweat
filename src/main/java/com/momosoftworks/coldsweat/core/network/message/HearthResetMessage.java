package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class HearthResetMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<HearthResetMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth_reset"));
    public static final StreamCodec<FriendlyByteBuf, HearthResetMessage> CODEC = CustomPacketPayload.codec(HearthResetMessage::encode, HearthResetMessage::decode);

    BlockPos blockPos;

    public HearthResetMessage(BlockPos blockPos)
    {   this.blockPos = blockPos;
    }

    public void encode(FriendlyByteBuf buffer)
    {   buffer.writeBlockPos(this.blockPos);
    }

    public static HearthResetMessage decode(FriendlyByteBuf buffer)
    {   return new HearthResetMessage(buffer.readBlockPos());
    }

    public static void handle(HearthResetMessage message, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            BlockEntity te = Minecraft.getInstance().level.getBlockEntity(message.blockPos);
            if (te instanceof HearthBlockEntity hearth)
            {   hearth.forceUpdate();
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}