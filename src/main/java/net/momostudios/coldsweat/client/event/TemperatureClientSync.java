package net.momostudios.coldsweat.client.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.util.PlayerHelper;
import net.momostudios.coldsweat.util.PlayerTemp;

@Mod.EventBusSubscriber
public class TemperatureClientSync
{
    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent event)
    {
        if (event.world.getGameTime() % 10 == 0 && !event.world.isRemote)
        {
            for (PlayerEntity player : event.world.getPlayers())
            {
                PlayerHelper.updateTemperature(player,
                        PlayerHelper.getTemperature(player, PlayerHelper.Types.BODY),
                        PlayerHelper.getTemperature(player, PlayerHelper.Types.BASE));
            }
        }

        if (event.world.getGameTime() % 60 == 0 && !event.world.isRemote)
        {
            for (PlayerEntity player : event.world.getPlayers())
            {
                PlayerHelper.updateModifiers(player);
            }
        }
    }
}
