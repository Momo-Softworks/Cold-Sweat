package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.entity.TempHelper;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID)
public class PlayerTempUpdater
{
    static int WORLD_TIME = 0;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            if (!event.player.level.isClientSide)
            {
                Player player = event.player;
                player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
                {
                    cap.tickUpdate(player);

                    if (player.tickCount % 60 == 0)
                    {
                        TempHelper.updateModifiers(player, cap);
                    }
                });
            }
            else
            {
                event.player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap -> cap.tickClient(event.player));
            }
        }
    }

    @SubscribeEvent
    public static void serverSyncConfigToCache(TickEvent.WorldTickEvent event)
    {
        // Syncs the server's config files to the cache
        if (!event.world.isClientSide && WORLD_TIME % 200 == 0)
            ConfigCache.getInstance().writeValues(ColdSweatConfig.getInstance());

        WORLD_TIME++;
    }
}