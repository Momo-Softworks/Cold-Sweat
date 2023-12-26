package com.momosoftworks.coldsweat.api.event.common;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.Style;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

public class ChatComponentClickedEvent extends Event
{
    private Style style;
    private ClientPlayerEntity player;
    private Screen screen;

    public ChatComponentClickedEvent(@Nullable Style style, ClientPlayerEntity player, Screen screen)
    {   this.style = style;
        this.player = player;
        this.screen = screen;
    }

    public Style getStyle()
    {   return style;
    }

    public ClientPlayerEntity getPlayer()
    {   return player;
    }

    public Screen getScreen()
    {   return screen;
    }
}
