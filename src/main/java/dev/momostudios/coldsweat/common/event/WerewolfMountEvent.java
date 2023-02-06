package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.util.compat.ModGetters;
import dev.momostudios.coldsweat.util.entity.compat.WerewolfModUtils;
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
        if (event.getTarget() instanceof Player werewolf && ModGetters.isWerewolvesLoaded()
        && WerewolfModUtils.isWerewolf(werewolf) && !WerewolfModUtils.isWerewolf(event.getPlayer()))
        {
            event.getPlayer().startRiding(werewolf);
        }
    }
}
