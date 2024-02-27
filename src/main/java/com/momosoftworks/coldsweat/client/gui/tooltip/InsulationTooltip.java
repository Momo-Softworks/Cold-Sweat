package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.Triplet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class InsulationTooltip extends Tooltip
{
    private static final ResourceLocation TOOLTIP = new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png");
    private static final ResourceLocation TOOLTIP_HC = new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar_hc.png");
    public static final Supplier<ResourceLocation> TOOLTIP_LOCATION = () ->
            ClientSettingsConfig.getInstance().isHighContrast() ? TOOLTIP_HC
                                                                : TOOLTIP;

    List<Insulation> insulation;
    InsulationType type;
     int width = 0;

    public InsulationTooltip(List<Insulation> insulation, InsulationType type)
    {   this.insulation = insulation;
        this.type = type;
    }

    @Override
    public int getHeight()
    {   return 10;
    }

    @Override
    public int getWidth(FontRenderer font)
    {   return width + 12;
    }

    @Override
    public void renderImage(FontRenderer font, int x, int y, MatrixStack matrixStack, ItemRenderer itemRenderer, int depth)
    {
        y += 12;
        Minecraft.getInstance().textureManager.bind(TOOLTIP_LOCATION.get());

        List<Insulation> posInsulation = new ArrayList<>();
        List<Insulation> negInsulation = new ArrayList<>();

        for (Insulation ins : insulation)
        {
            if (ins instanceof StaticInsulation)
            {
                StaticInsulation insul = (StaticInsulation) ins;
                double cold = insul.getCold();
                double hot = insul.getHot();

                if (CSMath.sign(cold) == CSMath.sign(hot))
                {
                    switch (CSMath.sign(cold))
                    {   case -1 : negInsulation.add(ins); break;
                        case 1 : posInsulation.add(ins); break;
                    }
                }
                else
                {
                    switch (CSMath.sign(cold))
                    {   case -1 : negInsulation.add(new StaticInsulation(-cold, 0)); break;
                        case 1 : posInsulation.add(new StaticInsulation(cold, 0)); break;
                    }
                    switch (CSMath.sign(hot))
                    {   case -1 : negInsulation.add(new StaticInsulation(0, hot)); break;
                        case 1 : posInsulation.add(new StaticInsulation(0, hot)); break;
                    }
                }
            }
            else if (ins instanceof AdaptiveInsulation)
            {
                AdaptiveInsulation adaptive = (AdaptiveInsulation) ins;
                double value = adaptive.getInsulation();
                if (value < 0)
                {   negInsulation.add(ins);
                }
                else
                {   posInsulation.add(ins);
                }
            }
        }

        /* Render Bars */
        matrixStack.pushPose();
        width = 0;

        // Positive insulation bar
        if (!posInsulation.isEmpty())
        {   renderBar(matrixStack, x, y, posInsulation, type, !negInsulation.isEmpty(), false);
            matrixStack.translate(posInsulation.size() * 6 + 12, 0, 0);
            width += posInsulation.size() * 6 + 12;
        }

        // Negative insulation bar
        if (!negInsulation.isEmpty())
        {   renderBar(matrixStack, x + width, y, negInsulation, type, true, true);
            width += negInsulation.size() * 6 + 12;
        }
        matrixStack.popPose();
    }

    static void renderCells(MatrixStack poseStack, int x, int y, int slots, double insulation, int uvX, boolean isAdaptive)
    {
        double rounded = CSMath.roundNearest(Math.abs(insulation), 0.25);
        for (int i = 0; i < slots; i++)
        {
            int uvY = isAdaptive
                      // If the amount of insulation in this cell is greater than 2, use the full cell texture, otherwise use the half cell texture
                      ? (rounded - i * 2 >= 2 ? 16 : 20)
                      : (rounded - i * 2 >= 2 ? 8 : 12);
            AbstractGui.blit(poseStack, x + i*6, y, 0, uvX, uvY, 6, 4, 32, 24);
        }
    }

    static void renderBar(MatrixStack poseStack, int x, int y, List<Insulation> insulations, InsulationType type, boolean showSign, boolean isNegative)
    {
        List<Insulation> sortedInsulation = Insulation.sort(insulations);

        // background
        for (int i = 0; i < insulations.size(); i++)
        {   AbstractGui.blit(poseStack, x + 7 + i * 6, y + 1, 0, 0, 0, 6, 4, 32, 24);
        }

        // slots
        poseStack.pushPose();
        for (Insulation insulation : sortedInsulation)
        {
            if (insulation instanceof AdaptiveInsulation)
            {
                AdaptiveInsulation adaptive = (AdaptiveInsulation) insulation;
                double value = adaptive.getInsulation();

                for (int i = 0; i < CSMath.ceil(Math.abs(value)) / 2; i++)
                {
                    double insul = CSMath.minAbs(CSMath.shrink(value, i * 2), 2);
                    // adaptive cells base
                    renderCells(poseStack, x + 7, y + 1, 1, insul, 12, true);

                    // adaptive cells overlay
                    double blend = Math.abs(adaptive.getFactor());
                    int overlayU;
                    switch (CSMath.sign(adaptive.getFactor()))
                    {   case -1 : overlayU = 6; break;
                        case 1  : overlayU = 18; break;
                        default : overlayU = 12; break;
                    };
                    RenderSystem.enableBlend();
                    RenderSystem.color4f(1, 1, 1, (float) blend);
                    renderCells(poseStack, x + 7, y + 1, 1, insul, overlayU, true);
                    RenderSystem.disableBlend();
                    RenderSystem.color4f(1, 1, 1, 1f);

                    poseStack.translate(6, 0, 0);
                }
            }
            else if (insulation instanceof StaticInsulation)
            {
                StaticInsulation staticInsulation = (StaticInsulation) insulation;
                double cold = staticInsulation.getCold();
                double hot = staticInsulation.getHot();
                double neutral = cold > 0 == hot > 0 ? CSMath.minAbs(cold, hot) : 0;
                if (cold == neutral) cold = 0;
                if (hot == neutral) hot = 0;

                // Cold insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(cold)) / 2; i++)
                {   double coldInsul = CSMath.minAbs(CSMath.shrink(cold, i * 2), 2);
                    renderCells(poseStack, x + 7, y + 1, 1, coldInsul, 12, false); // cold cells
                    poseStack.translate(6, 0, 0);
                }

                // Neutral insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(neutral)); i++)
                {   double neutralInsul = CSMath.minAbs(CSMath.shrink(neutral, i), 1) * 2;
                    renderCells(poseStack, x + 7, y + 1, 1, neutralInsul, 6, false); // neutral cells
                    poseStack.translate(6, 0, 0);
                }

                // Hot insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(hot)) / 2; i++)
                {   double hotInsul = CSMath.minAbs(CSMath.shrink(hot, i * 2), 2);
                    renderCells(poseStack, x + 7, y + 1, 1, hotInsul, 18, false); // hot cells
                    poseStack.translate(6, 0, 0);
                }
            }
        }
        poseStack.popPose();

        // border
        for (int i = 0; i < insulations.size(); i++)
        {
            boolean end = i == insulations.size() - 1;
            AbstractGui.blit(poseStack, x + 7 + i * 6, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 32, 24);
        }

        // icon
        switch (type)
        {
            case CURIO : AbstractGui.blit(poseStack, x, y - 1, 0, 24, 16, 8, 8, 32, 24); break;
            case ITEM  : AbstractGui.blit(poseStack, x, y - 1, 0, 24, 0, 8, 8, 32, 24); break;
            case ARMOR : AbstractGui.blit(poseStack, x, y - 1, 0, 24, 8, 8, 8, 32, 24); break;
        }

        if (showSign)
        {
            if (isNegative)
            {   // negative sign
                AbstractGui.blit(poseStack, x + 3, y + 3, 0, 19, 5, 5, 3, 32, 24);
            }
            else
            {   // positive sign
                AbstractGui.blit(poseStack, x + 3, y + 2, 0, 19, 0, 5, 5, 32, 24);
            }
        }
    }
}

