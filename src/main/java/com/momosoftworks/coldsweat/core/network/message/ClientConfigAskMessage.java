package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.core.network.ModPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class ClientConfigAskMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ClientConfigAskMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "client_config_ask"));
    public static final StreamCodec<FriendlyByteBuf, ClientConfigAskMessage> CODEC = CustomPacketPayload.codec(ClientConfigAskMessage::encode, ClientConfigAskMessage::decode);

    UUID openerUUID;

    public ClientConfigAskMessage(UUID openerUUID)
    {   this.openerUUID = openerUUID;
    }

    public ClientConfigAskMessage()
    {   this(SyncConfigSettingsMessage.EMPTY_UUID);
    }

    public void encode(FriendlyByteBuf buffer)
    {   buffer.writeUUID(this.openerUUID);
    }

    public static ClientConfigAskMessage decode(FriendlyByteBuf buffer)
    {   return new ClientConfigAskMessage(buffer.readUUID());
    }

    public static void handle(ClientConfigAskMessage message, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            if (context.player() instanceof ServerPlayer serverPlayer)
            {   PacketDistributor.sendToPlayer(serverPlayer, new SyncConfigSettingsMessage(message.openerUUID, serverPlayer.registryAccess()));
            }

        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}