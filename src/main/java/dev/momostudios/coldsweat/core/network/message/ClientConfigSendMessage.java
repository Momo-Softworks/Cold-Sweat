package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ClientConfigSendMessage
{
    ConfigCache configCache;

    public ClientConfigSendMessage(ConfigCache config)
    {
        this.configCache = config;
    }

    public static void encode(ClientConfigSendMessage message, FriendlyByteBuf buffer)
    {
        ColdSweatPacketHandler.writeConfigCacheToBuffer(message.configCache, buffer);
    }

    public static ClientConfigSendMessage decode(FriendlyByteBuf buffer)
    {
        return new ClientConfigSendMessage(ColdSweatPacketHandler.readConfigCacheFromBuffer(buffer));
    }

    public static void handle(ClientConfigSendMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isServer())
            {
                ColdSweatConfig.getInstance().writeValues(message.configCache);
                ColdSweatConfig.getInstance().save();

                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientConfigSendMessage(message.configCache));
            }

            ConfigCache.setInstance(message.configCache);
        });
        context.setPacketHandled(true);
    }
}
