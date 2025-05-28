package com.momosoftworks.coldsweat.api.event.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.eventbus.api.Event;

public class RenderHeartEvent extends Event
{
    PoseStack poseStack;
    Gui.HeartType heartType;
    int x;
    int y;
    int yOffset;
    boolean blink;
    boolean halfHeart;

    public RenderHeartEvent(PoseStack poseStack, Gui.HeartType heartType, int x, int y, int yOffset, boolean blink, boolean halfHeart)
    {
        this.poseStack = poseStack;
        this.heartType = heartType;
        this.x = x;
        this.y = y;
        this.yOffset = yOffset;
        this.blink = blink;
        this.halfHeart = halfHeart;
    }

    public PoseStack getPoseStack()
    {   return poseStack;
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
