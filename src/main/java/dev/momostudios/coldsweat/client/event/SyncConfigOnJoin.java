package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.ClientConfigAskMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SyncConfigOnJoin
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
}
