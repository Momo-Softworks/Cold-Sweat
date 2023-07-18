package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.core.init.TempModifierInit;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.ClientConfigAskMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientJoinSetup
{
    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        // Get configs
        ColdSweatPacketHandler.INSTANCE.sendToServer(new ClientConfigAskMessage());
        // Rebuild TempModifier registries
        TempModifierInit.buildRegistries();
    }
}
