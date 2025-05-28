package com.momosoftworks.coldsweat.api.event.vanilla;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.eventbus.api.Event;

public class RenderHeartEvent extends Event
{
    GuiGraphics guiGraphics;
    Gui.HeartType heartType;
    int x;
    int y;
    int yOffset;
    boolean blink;
    boolean halfHeart;

    public RenderHeartEvent(GuiGraphics guiGraphics, Gui.HeartType heartType, int x, int y, int yOffset, boolean blink, boolean halfHeart)
    {
        this.guiGraphics = guiGraphics;
        this.heartType = heartType;
        this.x = x;
        this.y = y;
        this.yOffset = yOffset;
        this.blink = blink;
        this.halfHeart = halfHeart;
    }

    public GuiGraphics getGuiGraphics()
    {   return guiGraphics;
    }
    public Gui.HeartType getHeartType()
    {   return heartType;
    }
    public int getX()
    {   return x;
    }
    public int getY()
    {   return y;
    }
    public int getYOffset()
    {   return yOffset;
    }
    public boolean isBlink()
    {   return blink;
    }
    public boolean isHalfHeart()
    {   return halfHeart;
    }
}
