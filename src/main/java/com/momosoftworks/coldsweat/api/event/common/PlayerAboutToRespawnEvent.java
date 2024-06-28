package com.momosoftworks.coldsweat.api.event.common;


import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Fired on the Forge bus when a player is about to respawn, <br>
 * unlike {@link net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent}, which fires after.
 */
public class PlayerAboutToRespawnEvent extends Event
{
    private final ServerPlayer player;
    private final ServerPlayer original;
    private final boolean returningFromEnd;

    public PlayerAboutToRespawnEvent(ServerPlayer player, ServerPlayer original, boolean returningFromEnd)
    {
        this.player = player;
        this.original = original;
        this.returningFromEnd = returningFromEnd;
    }

    public ServerPlayer getNewPlayer()
    {   return player;
    }

    public ServerPlayer getOldPlayer()
    {   return original;
    }

    public boolean isReturningFromEnd()
    {   return returningFromEnd;
    }
}
