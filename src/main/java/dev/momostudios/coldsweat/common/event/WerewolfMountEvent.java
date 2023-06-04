package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.util.compat.CompatManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
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
