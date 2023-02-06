package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.core.init.TempModifierInit;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.ClientConfigAskMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientJoinSetup
{
    static boolean GENERATED = false;

    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event)
    {
        if (!GENERATED && event.getWorld().isClientSide() && event.getEntity() == Minecraft.getInstance().player)
        {
            GENERATED = true;
            ColdSweatPacketHandler.INSTANCE.sendToServer(new ClientConfigAskMessage(false));
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveWorldEvent event)
    {
        if (GENERATED && event.getWorld().isClientSide() && event.getEntity() == Minecraft.getInstance().player)
        {
            GENERATED = false;
        }
    }

    @SubscribeEvent
    public static void registerClient(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        TempModifierInit.rebuildRegistries();
    }
}
