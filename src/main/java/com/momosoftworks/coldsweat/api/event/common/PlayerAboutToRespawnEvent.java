package com.momosoftworks.coldsweat.api.event.common;


import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the Forge bus when a player is about to respawn, <br>
 * unlike {@link net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent}, which fires after.
 */
public class PlayerAboutToRespawnEvent extends Event
{
    private final ServerPlayerEntity player;
    private final ServerPlayerEntity original;
    private final boolean returningFromEnd;

    public PlayerAboutToRespawnEvent(ServerPlayerEntity player, ServerPlayerEntity original, boolean returningFromEnd)
    {
        this.player = player;
        this.original = original;
        this.returningFromEnd = returningFromEnd;
    }

    public ServerPlayerEntity getNewPlayer()
    {   return player;
    }

    public ServerPlayerEntity getOldPlayer()
    {   return original;
    }

    public boolean isReturningFromEnd()
    {   return returningFromEnd;
    }
}
