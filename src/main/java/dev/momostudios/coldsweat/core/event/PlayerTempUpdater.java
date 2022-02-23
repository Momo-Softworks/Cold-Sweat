package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.PlayerHelper;

@Mod.EventBusSubscriber
public class PlayerTempUpdater
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && !event.player.world.isRemote)
        {
            if (event.player.ticksExisted % 20 == 0)
            {
                event.player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
                {
                    PlayerHelper.updateModifiers(event.player,
                            cap.getModifiers(PlayerHelper.Types.BODY),
                            cap.getModifiers(PlayerHelper.Types.BASE),
                            cap.getModifiers(PlayerHelper.Types.AMBIENT),
                            cap.getModifiers(PlayerHelper.Types.RATE));
                });
            }

            event.player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap -> cap.tickUpdate(event.player));
        }
    }


    @SubscribeEvent
    public static void serverSyncConfigToCache(TickEvent.WorldTickEvent event)
    {
        // Syncs the server's config files to the cache
        if (!event.world.isRemote && event.world.getGameTime() % 20 == 0)
            ConfigCache.getInstance().writeValues(ColdSweatConfig.getInstance());
    }
}