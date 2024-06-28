package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.util.compat.CompatManager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class WerewolfMountEvent
{
    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getTarget() instanceof Player werewolf && CompatManager.isWerewolvesLoaded()
        && CompatManager.isWerewolf(werewolf) && !CompatManager.isWerewolf(event.getEntity()))
        {
            event.getEntity().startRiding(werewolf);
        }
    }
}
