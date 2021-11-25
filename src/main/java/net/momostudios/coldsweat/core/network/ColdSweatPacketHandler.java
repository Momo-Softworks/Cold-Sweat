package net.momostudios.coldsweat.core.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.core.network.message.PlayerModifiersSyncMessage;
import net.momostudios.coldsweat.core.network.message.PlayerTempSyncMessage;
import net.momostudios.coldsweat.core.network.message.SoulLampInputClientMessage;
import net.momostudios.coldsweat.core.network.message.SoulLampInputMessage;

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
    }
}
