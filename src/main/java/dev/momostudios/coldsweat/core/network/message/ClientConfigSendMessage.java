package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraft.network.FriendlyByteBuf;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ClientConfigSendMessage
{
    ConfigSettings configSettings;

    public ClientConfigSendMessage(ConfigSettings config)
    {
        this.configSettings = config;
    }

    public static void encode(ClientConfigSendMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeNbt(ConfigHelper.writeConfigSettingsToNBT(message.configSettings));
    }

    public static ClientConfigSendMessage decode(FriendlyByteBuf buffer)
    {
        return new ClientConfigSendMessage(ConfigHelper.readConfigSettingsFromNBT(buffer.readNbt()));
    }

    public static void handle(ClientConfigSendMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isServer())
            {
                ColdSweatConfig.getInstance().writeValues(message.configSettings);
                ColdSweatConfig.getInstance().save();

                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientConfigSendMessage(message.configSettings));
            }

            ConfigSettings.setInstance(message.configSettings);
        });
        context.setPacketHandled(true);
    }
}
