package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InsulatorTooltip extends Tooltip
{
    Pair<Double, Double> insulationValues;
    double cold = 0;
    double hot = 0;
    double neutral = 0;
    boolean isAdaptive;
    int width = 0;

    public InsulatorTooltip(Pair<Double, Double> insulationValues, boolean isAdaptive)
    {
        this.insulationValues = insulationValues;
        this.isAdaptive = isAdaptive;
    }

    @Override
    public int getHeight()
    {
        return 10;
    }

    @Override
    public int getWidth(FontRenderer font)
    {
        return width + 12;
    }

    @Override
    public void renderImage(FontRenderer font, int x, int y, MatrixStack matrixStack, ItemRenderer itemRenderer, int depth)
    {
        y += 12;
        cold = insulationValues.getFirst();
        hot = insulationValues.getSecond();
        neutral = (cold > 0 == hot > 0 ? CSMath.minAbs(cold, hot) : 0) * 2;
        hot -= neutral/2;
        cold -= neutral/2;

        int coldSlots = Math.abs(CSMath.ceil(cold/2));
        int neutralSlots = Math.abs(CSMath.ceil(neutral/2));
        int hotSlots = Math.abs(CSMath.ceil(hot/2));
        width = coldSlots + neutralSlots + hotSlots;

        Minecraft.getInstance().textureManager.bind(new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png"));

        // Render positive and negative insulation separately
        int barLength = 0;
        int posSlots = isAdaptive ? coldSlots : CSMath.ceil(CSMath.max(coldSlots, hotSlots, neutralSlots));
        int negSlots = CSMath.ceil(Math.abs(CSMath.min(CSMath.ceil(cold/2), CSMath.ceil(hot/2), CSMath.ceil(neutral/2))));

        /* Render Bars */
        matrixStack.pushPose();
        width = 0;

        // Positive insulation bar
        if (posSlots > 0)
        {   renderBar(matrixStack, x, y, posSlots, cold, neutral, hot, isAdaptive, negSlots > 0, false);
            matrixStack.translate(posSlots * 6 + 12, 0, 0);
            width += posSlots * 6 + 12;
        }

        // Negative insulation bar
        if (negSlots > 0)
        {   renderBar(matrixStack, x, y, negSlots, -cold, -neutral, -hot, isAdaptive, true, true);
            width += negSlots * 6 + 12;
        }

        matrixStack.popPose();

        width = barLength;
    }

    static void renderCells(MatrixStack matrixStack, int x, int y, int slots, double insulation, int uvX, boolean isAdaptive)
    {
        double rounded = CSMath.round(Math.abs(insulation), 1);
        for (int i = 0; i < slots; i++)
        {
            int uvY = isAdaptive
                      ? (rounded - i * 2 >= 2 ? 16 : 20)
                      : (rounded - i * 2 >= 2 ? 8 : 12);
            AbstractGui.blit(matrixStack, x + i*6, y, 0, uvX, uvY, 6, 4, 24, 32);
        }
    }

    static void renderBar(MatrixStack matrixStack, int x, int y, int slots, double cold, double neutral, double hot, boolean isAdaptive, boolean showSign, boolean isNegative)
    {
        int coldSlots = Math.abs(CSMath.ceil(cold/2));
        int neutralSlots = Math.abs(CSMath.ceil(neutral/2));
        int hotSlots = Math.abs(CSMath.ceil(hot/2));

        // background
        for (int i = 0; i < slots; i++)
        {   AbstractGui.blit(matrixStack, x + 7 + i * 6, y + 1, 0, 0, 0, 6, 4, 24, 32);
        }

        // slots
        if (isAdaptive)
        {   renderCells(matrixStack, x + 7, y + 1, coldSlots, cold, 12, true); // adaptive cells
        }
        else
        {
            int xOffs = 0;
            if (cold > 0)
            {   renderCells(matrixStack, x + 7, y + 1, coldSlots, cold, 12, false); // cold cells
                xOffs += coldSlots * 6;
            }
            if (neutral > 0)
            {   renderCells(matrixStack, x + 7 + xOffs, y + 1, neutralSlots, neutral, 6, false); // neutral cells
                xOffs += neutralSlots * 6;
            }
            if (hot > 0)
            {   renderCells(matrixStack, x + 7 + xOffs, y + 1, hotSlots, hot, 18, false); // hot cells
            }
        }

        // border
        for (int i = 0; i < slots; i++)
        {
            boolean end = i == slots - 1;
            AbstractGui.blit(matrixStack, x + 7 + i * 6, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 24, 32);
        }

        // icon
        AbstractGui.blit(matrixStack, x, y - 1, 0, 24, 0, 8, 8, 24, 32);

        if (showSign)
        {
            if (isNegative)
            {   // negative sign
                AbstractGui.blit(matrixStack, x + 3, y + 3, 0, 19, 5, 5, 3, 24, 32);
            }
            else
            {   // positive sign
                AbstractGui.blit(matrixStack, x + 3, y + 2, 0, 19, 0, 5, 5, 24, 32);
            }
        }
    }
}
