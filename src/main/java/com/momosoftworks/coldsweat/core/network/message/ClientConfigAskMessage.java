package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientConfigAskMessage
{
    UUID openerUUID;

    public ClientConfigAskMessage(UUID openerUUID)
    {
        this.openerUUID = openerUUID;
    }

    public ClientConfigAskMessage()
    {
        this(SyncConfigSettingsMessage.EMPTY_UUID);
    }

    public static void encode(ClientConfigAskMessage message, PacketBuffer buffer) {
        buffer.writeUUID(message.openerUUID);
    }

    public static ClientConfigAskMessage decode(PacketBuffer buffer)
    {
        return new ClientConfigAskMessage(buffer.readUUID());
    }

    public static void handle(ClientConfigAskMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isServer())
            {
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(context::getSender), new SyncConfigSettingsMessage(message.openerUUID, context.getSender().level.registryAccess()));
            }
        });
        context.setPacketHandled(true);
    }
}