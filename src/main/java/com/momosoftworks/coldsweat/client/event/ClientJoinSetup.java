package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.api.event.core.registry.EdiblesRegisterEvent;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import com.momosoftworks.coldsweat.core.network.message.ClientConfigAskMessage;
import com.momosoftworks.coldsweat.core.network.message.SyncPreferredUnitsMessage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(Dist.CLIENT)
public class ClientJoinSetup
{
    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event)
    {
        // Get configs
        PacketDistributor.sendToServer(new ClientConfigAskMessage());
        PacketDistributor.sendToServer(new SyncPreferredUnitsMessage(ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F));
        // Rebuild TempModifier registries
        TempModifierInit.buildModifierRegistries();
        NeoForge.EVENT_BUS.post(new EdiblesRegisterEvent());
    }
}
