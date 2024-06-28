package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.entity.Chameleon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ChameleonEatMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ChameleonEatMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "chameleon_eat"));
    public static final StreamCodec<FriendlyByteBuf, ChameleonEatMessage> CODEC = CustomPacketPayload.codec(ChameleonEatMessage::encode, ChameleonEatMessage::decode);

    int entityId;

    public ChameleonEatMessage(int entityId)
    {   this.entityId = entityId;
    }

    public void encode(FriendlyByteBuf buffer)
    {   buffer.writeInt(this.entityId);
    }

    public static ChameleonEatMessage decode(FriendlyByteBuf buffer)
    {   return new ChameleonEatMessage(buffer.readInt());
    }

    public static void handle(ChameleonEatMessage message, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            if (Minecraft.getInstance().level.getEntity(message.entityId) instanceof Chameleon chameleon)
            {   chameleon.eatAnimation();
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}