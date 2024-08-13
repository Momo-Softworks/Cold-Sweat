package com.momosoftworks.coldsweat.util.render;

import net.minecraft.util.math.MathHelper;

public class ChestLidController
{
    private boolean shouldBeOpen;
    private float openness;
    private float oOpenness;

    public void tickLid()
    {
        this.oOpenness = this.openness;
        float f = 0.1F;
        if (!this.shouldBeOpen && this.openness > 0.0F)
        {   this.openness = Math.max(this.openness - 0.1F, 0.0F);
        }
        else if (this.shouldBeOpen && this.openness < 1.0F)
        {   this.openness = Math.min(this.openness + 0.1F, 1.0F);
        }
    }

    public float getOpenness(float pPartialTicks)
    {   return MathHelper.lerp(pPartialTicks, this.oOpenness, this.openness);
    }

    public void shouldBeOpen(boolean pShouldBeOpen)
    {   this.shouldBeOpen = pShouldBeOpen;
    }
}
