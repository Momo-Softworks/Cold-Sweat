package com.momosoftworks.coldsweat.api.event.common;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

public class ChatComponentClickedEvent extends Event
{
    private Style style;
    private LocalPlayer player;
    private Screen screen;

    public ChatComponentClickedEvent(@Nullable Style style, LocalPlayer player, Screen screen)
    {   this.style = style;
        this.player = player;
        this.screen = screen;
    }

    public Style getStyle()
    {   return style;
    }

    public LocalPlayer getPlayer()
    {   return player;
    }

    public Screen getScreen()
    {   return screen;
    }
}
