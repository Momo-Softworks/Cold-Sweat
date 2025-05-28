package com.momosoftworks.coldsweat.api.event.vanilla;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.eventbus.api.Event;

public class RenderHeartEvent extends Event
{
    MatrixStack poseStack;
    HeartType heartType;
    int x;
    int y;
    boolean blink;
    boolean halfHeart;

    public RenderHeartEvent(MatrixStack poseStack, HeartType heartType, int x, int y, boolean blink, boolean halfHeart)
    {
        this.poseStack = poseStack;
        this.heartType = heartType;
        this.x = x;
        this.y = y;
        this.blink = blink;
        this.halfHeart = halfHeart;
    }

    public MatrixStack getPoseStack()
    {   return poseStack;
    }
    public HeartType getHeartType()
    {   return heartType;
    }
    public int getX()
    {   return x;
    }
    public int getY()
    {   return y;
    }
    public boolean isBlink()
    {   return blink;
    }
    public boolean isHalfHeart()
    {   return halfHeart;
    }

    public enum HeartType
    {
        CONTAINER,
        NORMAL,
        POISONED,
        WITHERED,
        ABSORBING
    }
}
