package dev.momostudios.coldsweat.util.entity.compat;

import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import net.minecraft.world.entity.player.Player;

public class WerewolfModUtils
{
    public static boolean isWerewolf(Player player)
    {
        return WerewolfPlayer.getOpt(player).filter(w -> w.getLevel() > 0).map(w -> w.getForm().isTransformed()).orElse(false);
    }
}
