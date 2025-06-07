package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.api.event.core.registry.EdiblesRegisterEvent;
import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.ClientConfigAskMessage;
import com.momosoftworks.coldsweat.core.network.message.SyncPreferencesMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
        TempModifierInit.buildModifierRegistries();
        MinecraftForge.EVENT_BUS.post(new EdiblesRegisterEvent());
    }

    @SubscribeEvent
    public static void onEverySpawn(EntityJoinWorldEvent event)
    {
        if (event.getEntity() == Minecraft.getInstance().player)
        {   ColdSweatPacketHandler.INSTANCE.sendToServer(SyncPreferencesMessage.create());
        }
    }
}
