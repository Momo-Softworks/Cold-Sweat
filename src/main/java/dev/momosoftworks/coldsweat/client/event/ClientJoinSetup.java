package dev.momosoftworks.coldsweat.client.event;

import dev.momosoftworks.coldsweat.core.init.TempModifierInit;
import dev.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momosoftworks.coldsweat.core.network.message.ClientConfigAskMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientJoinSetup
{
    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event)
    {
        // Get configs
        ColdSweatPacketHandler.INSTANCE.sendToServer(new ClientConfigAskMessage());
        // Rebuild TempModifier registries
        TempModifierInit.buildRegistries();
    }
}
