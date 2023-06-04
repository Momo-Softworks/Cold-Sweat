package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.core.init.TempModifierInit;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.ClientConfigAskMessage;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientJoinSetup
{
    static boolean GENERATED = false;

    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event)
    {
        if (!GENERATED)
        {
            GENERATED = true;
            ColdSweatPacketHandler.INSTANCE.sendToServer(new ClientConfigAskMessage());
        }

        ConfigSettings.SYNCED_SETTINGS.forEach((key, value) -> value.load());
        TempModifierInit.rebuildRegistries();
    }

    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut event)
    {
        if (GENERATED)
        {
            GENERATED = false;
        }
    }
}
