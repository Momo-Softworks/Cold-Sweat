package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.core.network.message.ClientConfigAskMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.config.WorldTemperatureConfig;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SyncConfigOnJoin
{
    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event)
    {
        if (event.getEntity() == Minecraft.getInstance().player)
        {
            if (!Minecraft.getInstance().isLocalServer())
            {
                ColdSweatPacketHandler.INSTANCE.sendToServer(new ClientConfigAskMessage(true));
            }
            else
            {
                ConfigCache.getInstance().writeValues(ColdSweatConfig.getInstance());
                ConfigCache.getInstance().worldOptionsReference.putAll(WorldTemperatureConfig.INSTANCE.getConfigMap());
            }
        }
    }
}
