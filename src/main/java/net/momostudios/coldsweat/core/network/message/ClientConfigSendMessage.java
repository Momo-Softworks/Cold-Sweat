package net.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;

import java.util.function.Supplier;

public class ClientConfigSendMessage
{
    ConfigCache configCache;

    public ClientConfigSendMessage(ConfigCache config)
    {
        this.configCache = config;
    }

    public static void encode(ClientConfigSendMessage message, PacketBuffer buffer)
    {
        ColdSweatPacketHandler.writeConfigCacheToBuffer(message.configCache, buffer);
    }

    public static ClientConfigSendMessage decode(PacketBuffer buffer)
    {
        return new ClientConfigSendMessage(ColdSweatPacketHandler.readConfigCacheFromBuffer(buffer));
    }

    public static void handle(ClientConfigSendMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (Minecraft.getInstance().player == null)
                ColdSweatConfig.getInstance().writeValues(message.configCache);

            ConfigCache.setInstance(message.configCache);
        });
    }
}
