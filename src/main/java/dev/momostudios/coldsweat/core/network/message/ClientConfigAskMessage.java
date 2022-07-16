package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ClientConfigAskMessage
{
    boolean openMenu;

    public ClientConfigAskMessage(boolean openMenu) {
        this.openMenu = openMenu;
    }

    public static void encode(ClientConfigAskMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.openMenu);
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
            cache.readValues(ColdSweatConfig.getInstance());

            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(context::getSender), new ClientConfigRecieveMessage(cache, message.openMenu));
        });
        context.setPacketHandled(true);
    }
}