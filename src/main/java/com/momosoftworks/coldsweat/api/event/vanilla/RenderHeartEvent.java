package com.momosoftworks.coldsweat.api.event.vanilla;

import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.bus.api.Event;

public class RenderHeartEvent extends Event
{
    GuiGraphicsExtractor guiGraphics;
    Hud.HeartType heartType;
    int x;
    int y;
    boolean hardcode;
    boolean blink;
    boolean halfHeart;

    public RenderHeartEvent(GuiGraphicsExtractor guiGraphics, Hud.HeartType heartType, int x, int y, boolean hardcode, boolean blink, boolean halfHeart)
    {
        this.guiGraphics = guiGraphics;
        this.heartType = heartType;
        this.x = x;
        this.y = y;
        this.hardcode = hardcode;
        this.blink = blink;
        this.halfHeart = halfHeart;
    }

    public GuiGraphicsExtractor getGuiGraphics()
    {   return guiGraphics;
    }
    public Hud.HeartType getHeartType()
    {   return heartType;
    }
    public int getX()
    {   return x;
    }
    public int getY()
    {   return y;
    }
    public boolean isHardcode()
    {   return hardcode;
    }
    public boolean isBlink()
    {   return blink;
    }
    public boolean isHalfHeart()
    {   return halfHeart;
    }
}
