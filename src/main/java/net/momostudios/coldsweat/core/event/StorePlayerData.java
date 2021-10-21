package net.momostudios.coldsweat.core.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.core.util.PlayerTemp;

@Mod.EventBusSubscriber
public class StorePlayerData
{
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        PlayerEntity player = event.getPlayer();
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            cap.set(PlayerTemp.Types.BODY, player.getPersistentData().getDouble("body_temperature"));
            cap.set(PlayerTemp.Types.BASE, player.getPersistentData().getDouble("base_temperature"));
        });
    }

    @SubscribeEvent
    public static void onLeave(PlayerEvent.PlayerLoggedOutEvent event)
    {
        PlayerEntity player = event.getPlayer();
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            player.getPersistentData().putDouble("body_temperature", cap.get(PlayerTemp.Types.BODY));
            player.getPersistentData().putDouble("base_temperature", cap.get(PlayerTemp.Types.BASE));
        });
    }
}
