package net.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.config.WorldTemperatureConfig;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.ClientConfigAskMessage;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SyncConfigOnJoin
{
    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event)
    {
        if (event.getEntity() == Minecraft.getInstance().player)
        {
            if (!Minecraft.getInstance().isSingleplayer())
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
