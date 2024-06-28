package com.momosoftworks.coldsweat.core.network;

import com.google.common.eventbus.Subscribe;
import com.momosoftworks.coldsweat.core.network.message.SyncForgeDataMessage;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class ModPacketHandlers
{
    public static final String NETWORK_VERSION = "1";

    @Subscribe
    public static void registerPackets(RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToClient(SyncForgeDataMessage.TYPE,
                               SyncForgeDataMessage.CODEC,
                               SyncForgeDataMessage::handle);
    }
}
