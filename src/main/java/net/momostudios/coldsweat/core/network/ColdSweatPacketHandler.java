package net.momostudios.coldsweat.core.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.core.network.message.*;

public class ColdSweatPacketHandler
{
    private static final String PROTOCOL_VERSION = "0.1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ColdSweat.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init()
    {
        INSTANCE.registerMessage(0, PlayerTempSyncMessage.class, PlayerTempSyncMessage::encode, PlayerTempSyncMessage::decode, PlayerTempSyncMessage::handle);
        INSTANCE.registerMessage(1, PlayerModifiersSyncMessage.class, PlayerModifiersSyncMessage::encode, PlayerModifiersSyncMessage::decode, PlayerModifiersSyncMessage::handle);
        INSTANCE.registerMessage(2, SoulLampInputMessage.class, SoulLampInputMessage::encode, SoulLampInputMessage::decode, SoulLampInputMessage::handle);
        INSTANCE.registerMessage(3, SoulLampInputClientMessage.class, SoulLampInputClientMessage::encode, SoulLampInputClientMessage::decode, SoulLampInputClientMessage::handle);
        INSTANCE.registerMessage(4, ClientConfigSendMessage.class, ClientConfigSendMessage::encode, ClientConfigSendMessage::decode, ClientConfigSendMessage::handle);
        INSTANCE.registerMessage(5, ClientConfigAskMessage.class, ClientConfigAskMessage::encode, ClientConfigAskMessage::decode, ClientConfigAskMessage::handle);
        INSTANCE.registerMessage(6, ClientConfigRecieveMessage.class, ClientConfigRecieveMessage::encode, ClientConfigRecieveMessage::decode, ClientConfigRecieveMessage::handle);
    }
    
    public static void writeConfigCacheToBuffer(ConfigCache config, PacketBuffer buffer)
    {
        buffer.writeInt(config.difficulty);
        buffer.writeDouble(config.minTemp);
        buffer.writeDouble(config.maxTemp);
        buffer.writeDouble(config.rate);
        buffer.writeBoolean(config.fireRes);
        buffer.writeBoolean(config.iceRes);
        buffer.writeBoolean(config.damageScaling);
        buffer.writeBoolean(config.showAmbient);
    }

    public static ConfigCache readConfigCacheFromBuffer(PacketBuffer buffer)
    {
        ConfigCache config = new ConfigCache();
        config.difficulty = buffer.readInt();
        config.minTemp = buffer.readDouble();
        config.maxTemp = buffer.readDouble();
        config.rate = buffer.readDouble();
        config.fireRes = buffer.readBoolean();
        config.iceRes = buffer.readBoolean();
        config.damageScaling = buffer.readBoolean();
        config.showAmbient = buffer.readBoolean();
        return config;
    }
}
