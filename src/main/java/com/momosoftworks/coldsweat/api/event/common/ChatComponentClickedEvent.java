package com.momosoftworks.coldsweat.api.event.common;

import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;

public class ChatComponentClickedEvent extends Event
{
    private Style style;
    private Player player;

    public ChatComponentClickedEvent(@Nullable Style style, Player player)
    {   this.style = style;
        this.player = player;
    }

    public Style getStyle()
    {   return style;
    }

    public Player getPlayer()
    {   return player;
    }
}
