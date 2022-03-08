package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.entity.PlayerHelper;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID)
public class PlayerTempUpdater
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && !event.player.level.isClientSide)
        {
            if (event.player.tickCount % 20 == 0)
            {
                event.player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
                {
                    PlayerHelper.updateModifiers(event.player,
                            cap.getModifiers(Temperature.Types.BODY),
                            cap.getModifiers(Temperature.Types.BASE),
                            cap.getModifiers(Temperature.Types.WORLD),
                            cap.getModifiers(Temperature.Types.RATE));
                });
            }

            event.player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap -> cap.tickUpdate(event.player));
        }
    }

    @SubscribeEvent
    public static void serverSyncConfigToCache(TickEvent.WorldTickEvent event)
    {
        // Syncs the server's config files to the cache
        if (!event.world.isClientSide && event.world.getGameTime() % 20 == 0)
            ConfigCache.getInstance().writeValues(ColdSweatConfig.getInstance());
    }
}