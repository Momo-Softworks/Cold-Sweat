package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.api.event.core.registry.EdiblesRegisterEvent;
import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import com.momosoftworks.coldsweat.core.network.message.ClientConfigAskMessage;
import com.momosoftworks.coldsweat.core.network.message.SyncPreferencesMessage;
import com.momosoftworks.coldsweat.data.ModRegistries;
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
        ModRegistries.fillOptionalHolders(event.getPlayer().connection.registryAccess());
        // Get configs
        PacketDistributor.sendToServer(new ClientConfigAskMessage());
        // Rebuild TempModifier registries
        TempModifierInit.buildModifierRegistries();
        NeoForge.EVENT_BUS.post(new EdiblesRegisterEvent());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut event)
    {   ModRegistries.OPTIONAL_HOLDERS.clear();
    }

    @SubscribeEvent
    public static void onEverySpawn(EntityJoinLevelEvent event)
    {
        if (event.getEntity() == Minecraft.getInstance().player)
        {   PacketDistributor.sendToServer(SyncPreferencesMessage.create());
        }
    }
}
