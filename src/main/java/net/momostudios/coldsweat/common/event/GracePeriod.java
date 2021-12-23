package net.momostudios.coldsweat.common.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.GracePeriodAskMessage;
import net.momostudios.coldsweat.core.util.registrylists.ModEffects;

@Mod.EventBusSubscriber
public class GracePeriod
{
    @SubscribeEvent
    public static void onSpawn(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity && ConfigCache.getInstance().gracePeriodEnabled)
        {
            if (event.getEntity().getServer() != null && !event.getEntity().world.isRemote)
            {
                ColdSweatPacketHandler.INSTANCE.sendToServer(new GracePeriodAskMessage(6000));
            }
        }
    }
}
