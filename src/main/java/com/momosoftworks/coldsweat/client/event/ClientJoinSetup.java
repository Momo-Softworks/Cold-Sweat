package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

public class ClientJoinSetup
{
    @SubscribeEvent
    public void onJoin(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        // Get configs
        // TODO: 10/5/23 Add this back when config screen is made
        //ColdSweatPacketHandler.INSTANCE.sendToServer(new ClientConfigAskMessage());
        // Rebuild TempModifier registries
        TempModifierInit.buildRegistries();
    }
}
