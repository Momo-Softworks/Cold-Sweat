package com.momosoftworks.coldsweat.core.network;

import com.momosoftworks.coldsweat.core.network.message.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModPacketHandlers
{
    public static final String NETWORK_VERSION = "1";

    @SubscribeEvent
    public static void registerPackets(RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);

        registrar.playToClient(BlockDataUpdateMessage.TYPE, BlockDataUpdateMessage.CODEC, BlockDataUpdateMessage::handle);
        registrar.playToClient(ChameleonEatMessage.TYPE, ChameleonEatMessage.CODEC, ChameleonEatMessage::handle);
        registrar.playToServer(ClientConfigAskMessage.TYPE, ClientConfigAskMessage.CODEC, ClientConfigAskMessage::handle);
        registrar.playBidirectional(DisableHearthParticlesMessage.TYPE, DisableHearthParticlesMessage.CODEC, DisableHearthParticlesMessage::handle);
        registrar.playToClient(HearthResetMessage.TYPE, HearthResetMessage.CODEC, HearthResetMessage::handle);
        registrar.playToClient(ParticleBatchMessage.TYPE, ParticleBatchMessage.CODEC, ParticleBatchMessage::handle);
        registrar.playToClient(PlayEntityAttachedSoundMessage.TYPE, PlayEntityAttachedSoundMessage.CODEC, PlayEntityAttachedSoundMessage::handle);
        registrar.playBidirectional(SyncConfigSettingsMessage.TYPE, SyncConfigSettingsMessage.CODEC, SyncConfigSettingsMessage::handle);
        registrar.playToClient(SyncContainerSlotMessage.TYPE, SyncContainerSlotMessage.CODEC, SyncContainerSlotMessage::handle);
        registrar.playToClient(SyncForgeDataMessage.TYPE, SyncForgeDataMessage.CODEC, SyncForgeDataMessage::handle);
        registrar.playToServer(SyncPreferredUnitsMessage.TYPE, SyncPreferredUnitsMessage.CODEC, SyncPreferredUnitsMessage::handle);
        registrar.playToClient(SyncShearableDataMessage.TYPE, SyncShearableDataMessage.CODEC, SyncShearableDataMessage::handle);
        registrar.playToClient(SyncTemperatureMessage.TYPE, SyncTemperatureMessage.CODEC, SyncTemperatureMessage::handle);
        registrar.playToClient(SyncTempModifiersMessage.TYPE, SyncTempModifiersMessage.CODEC, SyncTempModifiersMessage::handle);
        registrar.playToClient(EntityMountMessage.TYPE, EntityMountMessage.CODEC, EntityMountMessage::handle);
    }
}
