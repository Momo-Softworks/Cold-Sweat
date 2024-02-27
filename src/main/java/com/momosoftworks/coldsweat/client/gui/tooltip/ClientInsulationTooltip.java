package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientInsulationTooltip implements ClientTooltipComponent
{
    public static final ResourceLocation TOOLTIP = new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png");
    public static final ResourceLocation TOOLTIP_HC = new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar_hc.png");
    public static final Supplier<ResourceLocation> TOOLTIP_LOCATION = () ->
            ConfigSettings.HIGH_CONTRAST.get() ? TOOLTIP_HC
                                               : TOOLTIP;

    List<Insulation> insulation;
    InsulationType type;
    int width = 0;

    public ClientInsulationTooltip(List<Insulation> insulation, InsulationType type)
    {   this.insulation = insulation;
        this.type = type;
    }

    @Override
    public int getHeight()
    {   return 10;
    }

    @Override
    public int getWidth(Font font)
    {   return width + 12;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics)
    {
        PoseStack poseStack = graphics.pose();
        List<Insulation> posInsulation = new ArrayList<>();
        List<Insulation> negInsulation = new ArrayList<>();

        for (Insulation ins : insulation)
        {
            if (ins instanceof StaticInsulation insul)
            {
                double cold = insul.getCold();
                double hot = insul.getHot();

                if (CSMath.sign(cold) == CSMath.sign(hot))
                {
                    switch (CSMath.sign(cold))
                    {   case -1 -> negInsulation.add(ins);
                        case 1 -> posInsulation.add(ins);
                    }
                }
                else
                {
                    switch (CSMath.sign(cold))
                    {   case -1 -> negInsulation.add(new StaticInsulation(-cold, 0));
                        case 1 -> posInsulation.add(new StaticInsulation(cold, 0));
                    }
                    switch (CSMath.sign(hot))
                    {   case -1 -> negInsulation.add(new StaticInsulation(0, hot));
                        case 1 -> posInsulation.add(new StaticInsulation(0, hot));
                    }
                }
            }
            else if (ins instanceof AdaptiveInsulation adaptive)
            {
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
        poseStack.pushPose();
        width = 0;

        // Positive insulation bar
        if (!posInsulation.isEmpty())
        {   renderBar(graphics, x, y, posInsulation, type, !negInsulation.isEmpty(), false);
            poseStack.translate(posInsulation.size() * 6 + 12, 0, 0);
            width += posInsulation.size() * 6 + 12;
        }

        // Negative insulation bar
        if (!negInsulation.isEmpty())
        {   renderBar(graphics, x + width, y, negInsulation, type, true, true);
            width += negInsulation.size() * 6 + 12;
        }
        poseStack.popPose();
    }

    static void renderCells(GuiGraphics graphics, int x, int y, int slots, double insulation, int uvX, boolean isAdaptive)
    {
        double rounded = CSMath.roundNearest(Math.abs(insulation), 0.25);
        for (int i = 0; i < slots; i++)
        {
            int uvY = isAdaptive
                      // If the amount of insulation in this cell is greater than 2, use the full cell texture, otherwise use the half cell texture
                      ? (rounded - i * 2 >= 2 ? 16 : 20)
                      : (rounded - i * 2 >= 2 ? 8 : 12);
            graphics.blit(TOOLTIP_LOCATION.get(), x + i*6, y, 0, uvX, uvY, 6, 4, 32, 24);
        }
    }

    static void renderBar(GuiGraphics graphics, int x, int y, List<Insulation> insulations, InsulationType type, boolean showSign, boolean isNegative)
    {
        PoseStack poseStack = graphics.pose();
        List<Insulation> sortedInsulation = Insulation.sort(insulations);

        // background
        for (int i = 0; i < insulations.size(); i++)
        {   graphics.blit(TOOLTIP_LOCATION.get(), x + 7 + i * 6, y + 1, 0, 0, 0, 6, 4, 32, 24);
        }

        // slots
        poseStack.pushPose();
        for (Insulation insulation : sortedInsulation)
        {
            if (insulation instanceof AdaptiveInsulation adaptive)
            {
                double value = adaptive.getInsulation();

                for (int i = 0; i < CSMath.ceil(Math.abs(value)) / 2; i++)
                {
                    double insul = CSMath.minAbs(CSMath.shrink(value, i * 2), 2);
                    // adaptive cells base
                    renderCells(graphics, x + 7, y + 1, 1, insul, 12, true);

                    // adaptive cells overlay
                    double blend = Math.abs(adaptive.getFactor());
                    int overlayU = switch (CSMath.sign(adaptive.getFactor()))
                    {   case -1 -> 6;
                        case 1 -> 18;
                        default -> 12;
                    };
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1, 1, 1, (float) blend);
                    renderCells(graphics, x + 7, y + 1, 1, insul, overlayU, true);
                    RenderSystem.disableBlend();
                    RenderSystem.setShaderColor(1, 1, 1, 1f);

                    poseStack.translate(6, 0, 0);
                }
            }
            else if (insulation instanceof StaticInsulation staticInsulation)
            {
                double cold = staticInsulation.getCold();
                double hot = staticInsulation.getHot();
                double neutral = cold > 0 == hot > 0 ? CSMath.minAbs(cold, hot) : 0;
                if (cold == neutral) cold = 0;
                if (hot == neutral) hot = 0;

                // Cold insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(cold)) / 2; i++)
                {   double coldInsul = CSMath.minAbs(CSMath.shrink(cold, i * 2), 2);
                    renderCells(graphics, x + 7, y + 1, 1, coldInsul, 12, false); // cold cells
                    poseStack.translate(6, 0, 0);
                }

                // Neutral insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(neutral)); i++)
                {   double neutralInsul = CSMath.minAbs(CSMath.shrink(neutral, i), 1) * 2;
                    renderCells(graphics, x + 7, y + 1, 1, neutralInsul, 6, false); // neutral cells
                    poseStack.translate(6, 0, 0);
                }

                // Hot insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(hot)) / 2; i++)
                {   double hotInsul = CSMath.minAbs(CSMath.shrink(hot, i * 2), 2);
                    renderCells(graphics, x + 7, y + 1, 1, hotInsul, 18, false); // hot cells
                    poseStack.translate(6, 0, 0);
                }
            }
        }
        poseStack.popPose();

        // border
        for (int i = 0; i < insulations.size(); i++)
        {
            boolean end = i == insulations.size() - 1;
            graphics.blit(TOOLTIP_LOCATION.get(), x + 7 + i * 6, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 32, 24);
        }

        // icon
        switch (type)
        {
            case CURIO -> graphics.blit(TOOLTIP_LOCATION.get(), x, y - 1, 0, 24, 16, 8, 8, 32, 24);
            case ITEM -> graphics.blit(TOOLTIP_LOCATION.get(), x, y - 1, 0, 24, 0, 8, 8, 32, 24);
            case ARMOR -> graphics.blit(TOOLTIP_LOCATION.get(), x, y - 1, 0, 24, 8, 8, 8, 32, 24);
        }

        if (showSign)
        {
            if (isNegative)
            {   // negative sign
                graphics.blit(TOOLTIP_LOCATION.get(), x + 3, y + 3, 0, 19, 5, 5, 3, 32, 24);
            }
            else
            {   // positive sign
                graphics.blit(TOOLTIP_LOCATION.get(), x + 3, y + 2, 0, 19, 0, 5, 5, 32, 24);
            }
        }
    }
}
