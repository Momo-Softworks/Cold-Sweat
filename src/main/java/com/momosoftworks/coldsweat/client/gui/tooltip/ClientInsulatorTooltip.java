package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.momosoftworks.coldsweat.client.gui.tooltip.ClientInsulationTooltip.TOOLTIP_LOCATION;

@OnlyIn(Dist.CLIENT)
public class ClientInsulatorTooltip implements ClientTooltipComponent
{
    Pair<Double, Double> insulationValues;
    double cold = 0;
    double hot = 0;
    double neutral = 0;
    InsulatorTooltip.InsulationType type;
    int width = 0;

    public ClientInsulatorTooltip(Pair<Double, Double> insulationValues, InsulatorTooltip.InsulationType type)
    {
        this.insulationValues = insulationValues;
        this.type = type;
    }

    @Override
    public int getHeight()
    {
        return 10;
    }

    @Override
    public int getWidth(Font font)
    {
        return width + 12;
    }

    public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int depth)
    {
        boolean isAdaptive = this.type == InsulatorTooltip.InsulationType.ADAPTIVE;
        cold = insulationValues.getFirst();
        hot = insulationValues.getSecond();
        neutral = (cold > 0 == hot > 0 ? CSMath.minAbs(cold, hot) : 0) * 2;
        hot -= neutral/2;
        cold -= neutral/2;

        int coldSlots = Math.abs(CSMath.ceil(cold/2));
        int neutralSlots = Math.abs(CSMath.ceil(neutral/2));
        int hotSlots = Math.abs(CSMath.ceil(hot/2));
        width = coldSlots + neutralSlots + hotSlots;

        RenderSystem.setShaderTexture(0, TOOLTIP_LOCATION.get());

        // Render positive and negative insulation separately
        int barLength = 0;
        int posSlots = isAdaptive ? coldSlots : CSMath.ceil(CSMath.max(coldSlots, hotSlots, neutralSlots));
        int negSlots = CSMath.ceil(Math.abs(CSMath.min(CSMath.ceil(cold/2), CSMath.ceil(hot/2), CSMath.ceil(neutral/2))));

        /* Render Bars */
        poseStack.pushPose();
        width = 0;

        // Positive insulation bar
        if (posSlots > 0)
        {   renderBar(poseStack, x, y, posSlots, cold, neutral, hot, isAdaptive, negSlots > 0, false, this.type == InsulatorTooltip.InsulationType.CURIO);
            poseStack.translate(posSlots * 6 + 12, 0, 0);
            width += posSlots * 6 + 12;
        }

        // Negative insulation bar
        if (negSlots > 0)
        {   renderBar(poseStack, x, y, negSlots, -cold, -neutral, -hot, isAdaptive, true, true, this.type == InsulatorTooltip.InsulationType.CURIO);
            width += negSlots * 6 + 12;
        }

        poseStack.popPose();

        width = barLength;
    }

    static void renderCells(PoseStack poseStack, int x, int y, int slots, double insulation, int uvX, boolean isAdaptive)
    {
        double rounded = CSMath.round(Math.abs(insulation), 1);
        for (int i = 0; i < slots; i++)
        {
            int uvY = isAdaptive
                      ? (rounded - i * 2 >= 2 ? 16 : 20)
                      : (rounded - i * 2 >= 2 ? 8 : 12);
            GuiComponent.blit(poseStack, x + i*6, y, 0, uvX, uvY, 6, 4, 32, 24);
        }
    }

    static void renderBar(PoseStack poseStack, int x, int y, int slots, double cold, double neutral, double hot, boolean isAdaptive, boolean showSign, boolean isNegative, boolean isCurio)
    {
        int coldSlots = Math.abs(CSMath.ceil(cold/2));
        int neutralSlots = Math.abs(CSMath.ceil(neutral/2));
        int hotSlots = Math.abs(CSMath.ceil(hot/2));

        // background
        for (int i = 0; i < slots; i++)
        {   GuiComponent.blit(poseStack, x + 7 + i * 6, y + 1, 0, 0, 0, 6, 4, 32, 24);
        }

        // slots
        if (isAdaptive)
        {   renderCells(poseStack, x + 7, y + 1, coldSlots, cold, 12, true); // adaptive cells
        }
        else
        {
            int xOffs = 0;
            if (cold > 0)
            {   renderCells(poseStack, x + 7, y + 1, coldSlots, cold, 12, false); // cold cells
                xOffs += coldSlots * 6;
            }
            if (neutral > 0)
            {   renderCells(poseStack, x + 7 + xOffs, y + 1, neutralSlots, neutral, 6, false); // neutral cells
                xOffs += neutralSlots * 6;
            }
            if (hot > 0)
            {   renderCells(poseStack, x + 7 + xOffs, y + 1, hotSlots, hot, 18, false); // hot cells
            }
        }

        // border
        for (int i = 0; i < slots; i++)
        {
            boolean end = i == slots - 1;
            GuiComponent.blit(poseStack, x + 7 + i * 6, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 32, 24);
        }

        // icon
        if (isCurio)
        {   GuiComponent.blit(poseStack, x, y - 1, 0, 24, 16, 8, 8, 32, 24);
        }
        else
        {   GuiComponent.blit(poseStack, x, y - 1, 0, 24, 0, 8, 8, 32, 24);
        }

        if (showSign)
        {
            if (isNegative)
            {   // negative sign
                GuiComponent.blit(poseStack, x + 3, y + 3, 0, 19, 5, 5, 3, 32, 24);
            }
            else
            {   // positive sign
                GuiComponent.blit(poseStack, x + 3, y + 2, 0, 19, 0, 5, 5, 32, 24);
            }
        }
    }
}
