package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.util.entity.TempHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID)
public class PlayerTempUpdater
{
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
}