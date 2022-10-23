package dev.momostudios.coldsweat.core.network;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.core.network.message.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ColdSweatPacketHandler
{
    private static final String PROTOCOL_VERSION = "0.1.1";
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
        INSTANCE.registerMessage(2, ClientConfigSendMessage.class, ClientConfigSendMessage::encode, ClientConfigSendMessage::decode, ClientConfigSendMessage::handle);
        INSTANCE.registerMessage(3, ClientConfigAskMessage.class, ClientConfigAskMessage::encode, ClientConfigAskMessage::decode, ClientConfigAskMessage::handle);
        INSTANCE.registerMessage(4, ClientConfigRecieveMessage.class, ClientConfigRecieveMessage::encode, ClientConfigRecieveMessage::decode, ClientConfigRecieveMessage::handle);
        INSTANCE.registerMessage(5, PlaySoundMessage.class, PlaySoundMessage::encode, PlaySoundMessage::decode, PlaySoundMessage::handle);
        INSTANCE.registerMessage(6, BlockDataUpdateMessage.class, BlockDataUpdateMessage::encode, BlockDataUpdateMessage::decode, BlockDataUpdateMessage::handle);
        INSTANCE.registerMessage(7, HearthResetMessage.class, HearthResetMessage::encode, HearthResetMessage::decode, HearthResetMessage::handle);
        INSTANCE.registerMessage(8, DisableHearthParticlesMessage.class, DisableHearthParticlesMessage::encode, DisableHearthParticlesMessage::decode, DisableHearthParticlesMessage::handle);
        INSTANCE.registerMessage(9, ParticleBatchMessage.class, ParticleBatchMessage::encode, ParticleBatchMessage::decode, ParticleBatchMessage::handle);
    }
}
