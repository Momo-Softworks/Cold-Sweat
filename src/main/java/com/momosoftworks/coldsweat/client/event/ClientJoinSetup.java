package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.api.event.core.registry.EdiblesRegisterEvent;
import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import com.momosoftworks.coldsweat.core.network.message.ClientConfigAskMessage;
import com.momosoftworks.coldsweat.core.network.message.SyncPreferencesMessage;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(Dist.CLIENT)
public class ClientJoinSetup
{
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event)
    {
        // Build holders
        ConfigLoadingHandler.fillOptionalHolders(event.getPlayer().connection.registryAccess());
        // Get configs
        PacketDistributor.sendToServer(new ClientConfigAskMessage());
        // Rebuild TempModifier registries
        TempModifierInit.buildModifierRegistries();
        NeoForge.EVENT_BUS.post(new EdiblesRegisterEvent());
    }

    @SubscribeEvent
    public static void onEverySpawn(EntityJoinLevelEvent event)
    {
        if (event.getEntity() == Minecraft.getInstance().player)
        {   PacketDistributor.sendToServer(SyncPreferencesMessage.create());
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event)
    {

    }
}
