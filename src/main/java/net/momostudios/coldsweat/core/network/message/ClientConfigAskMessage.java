package net.momostudios.coldsweat.core.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.config.ItemSettingsConfig;
import net.momostudios.coldsweat.config.WorldTemperatureConfig;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;

import java.util.*;
import java.util.function.Supplier;

public class ClientConfigAskMessage
{
    boolean onJoin;

    public ClientConfigAskMessage(boolean onJoin) {
        this.onJoin = onJoin;
    }

    public static void encode(ClientConfigAskMessage message, PacketBuffer buffer) {
        buffer.writeBoolean(message.onJoin);
    }

    public static ClientConfigAskMessage decode(PacketBuffer buffer)
    {
        return new ClientConfigAskMessage(buffer.readBoolean());
    }

    public static void handle(ClientConfigAskMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            ConfigCache cache = new ConfigCache();
            cache.writeValues(ColdSweatConfig.getInstance());
            cache.itemSettingsReference = ItemSettingsConfig.INSTANCE;

            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(context::getSender), new ClientConfigRecieveMessage(cache, message.onJoin));
        });
    }
}