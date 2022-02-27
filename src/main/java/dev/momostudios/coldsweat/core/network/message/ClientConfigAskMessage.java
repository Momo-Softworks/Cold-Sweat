package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.config.WorldTemperatureConfig;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ClientConfigAskMessage
{
    boolean onJoin;

    public ClientConfigAskMessage(boolean onJoin) {
        this.onJoin = onJoin;
    }

    public static void encode(ClientConfigAskMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.onJoin);
    }

    public static ClientConfigAskMessage decode(FriendlyByteBuf buffer)
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
            cache.worldOptionsReference.putAll(WorldTemperatureConfig.INSTANCE.getConfigMap());
            cache.itemSettingsReference = ItemSettingsConfig.INSTANCE;

            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(context::getSender), new ClientConfigRecieveMessage(cache, message.onJoin));
        });
        context.setPacketHandled(true);
    }
}