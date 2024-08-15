package com.momosoftworks.coldsweat.api.event.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.Style;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

public class ChatComponentClickedEvent extends Event
{
    private final Style style;
    private final PlayerEntity player;

    public ChatComponentClickedEvent(@Nullable Style style, PlayerEntity player)
    {   this.style = style != null ? style : Style.EMPTY;
        this.player = player;
    }

    public Style getStyle()
    {   return style;
    }

    public PlayerEntity getPlayer()
    {   return player;
    }
}
