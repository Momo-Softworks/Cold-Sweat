package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientInsulationTooltip implements ClientTooltipComponent
{
    public static final ResourceLocation TOOLTIP = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/tooltip/insulation_bar.png");
    public static final ResourceLocation TOOLTIP_HC = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/tooltip/insulation_bar_hc.png");
    public static final Supplier<ResourceLocation> TOOLTIP_LOCATION = () ->
            ConfigSettings.HIGH_CONTRAST.get() ? TOOLTIP_HC
                                               : TOOLTIP;

    List<Insulation> insulation;
    Insulation.Slot type;
    int width = 0;
    ItemStack stack;

    public ClientInsulationTooltip(List<Insulation> insulation, Insulation.Slot type, ItemStack stack)
    {
        this.insulation = insulation;
        this.type = type;
        this.stack = stack;
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
                double heat = insul.getHeat();

                if (CSMath.sign(cold) == CSMath.sign(heat))
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
                    switch (CSMath.sign(heat))
                    {   case -1 -> negInsulation.add(new StaticInsulation(0, heat));
                        case 1 -> posInsulation.add(new StaticInsulation(0, heat));
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
        {   renderBar(graphics, x, y, posInsulation, type, !negInsulation.isEmpty(), false, stack);
            poseStack.translate(posInsulation.size() * 6 + 12, 0, 0);
            width += posInsulation.size() * 6 + 12;
        }

        // Negative insulation bar
        if (!negInsulation.isEmpty())
        {   renderBar(graphics, x + width, y, negInsulation, type, true, true, stack);
            width += negInsulation.size() * 6 + 12;
        }
        poseStack.popPose();
    }

    static void renderCell(GuiGraphics graphics, int x, int y, double insulation, int uvX, boolean isAdaptive)
    {
        double rounded = CSMath.roundNearest(Math.abs(insulation), 0.25);
        int uvY = isAdaptive
                  // If the amount of insulation in this cell is greater than 2, use the full cell texture, otherwise use the half cell texture
                  ? (rounded >= 2 ? 16 : 20)
                  : (rounded >= 2 ? 8 : 12);
        graphics.blit(TOOLTIP_LOCATION.get(), x, y, 0, uvX, uvY, 6, 4, 32, 24);
    }

    static int renderOverloadCell(GuiGraphics graphics, Font font, int x, int y, double insulation, int textColor, Insulation.Type type)
    {
        Number insul = CSMath.truncate(insulation / 2, 2);
        if (CSMath.isInteger(insul)) insul = insul.intValue();
        String text = "x" + insul;
        int uvX = switch (type)
        {   case COLD -> 12;
            case HEAT -> 18;
            case NEUTRAL -> 6;
            case ADAPTIVE -> 12;
        };

        renderCell(graphics, x + 7, y + 1, insulation, uvX, type == Insulation.Type.ADAPTIVE);
        graphics.blit(TOOLTIP_LOCATION.get(),
                      x + 6, y,
                      0, /*z*/
                      11 /*u*/, 0 /*v*/,
                      8 /*uWidth*/, 6 /*vHeight*/,
                      32, 24);
        graphics.drawString(font, text, x + 15, y - 1, textColor);
        // Return the width of the cell and text
        return 12 + font.width(text);
    }

    static void renderBar(GuiGraphics graphics, int x, int y, List<Insulation> insulations, Insulation.Slot type, boolean showSign, boolean isNegative, ItemStack stack)
    {
        PoseStack poseStack = graphics.pose();
        Font font = Minecraft.getInstance().font;
        List<Insulation> sortedInsulation = Insulation.sort(insulations);
        boolean overflow = sortedInsulation.size() >= 10;
        int insulSlotCount = Math.max(type == Insulation.Slot.ARMOR
                                  ? ConfigSettings.INSULATION_SLOTS.get().getSlots(Minecraft.getInstance().player.getEquipmentSlotForItem(stack), stack)
                                  : 0,
                                  insulations.size());

        // background
        for (int i = 0; i < insulSlotCount && !overflow; i++)
        {   graphics.blit(TOOLTIP_LOCATION.get(), x + 7 + i * 6, y + 1, 0, 0, 0, 6, 4, 32, 24);
        }

        // slots
        poseStack.pushPose();

        // If there's too much insulation, render a compact version of the tooltip
        if (overflow)
        {
            // tally up the insulation from the sorted list into cold, hot, neutral, and adaptive
            double cold = 0;
            double heat = 0;
            double neutral = 0;
            double adaptive = 0;
            for (Insulation insulation : sortedInsulation)
            {
                if (insulation instanceof StaticInsulation staticInsulation)
                {
                    if (staticInsulation.getCold() > staticInsulation.getHeat())
                        cold += staticInsulation.getCold();
                    else if (staticInsulation.getHeat() > staticInsulation.getCold())
                        heat += staticInsulation.getHeat();
                    else
                        neutral += staticInsulation.getCold() * 2;
                }
                else if (insulation instanceof AdaptiveInsulation adaptiveInsulation)
                {   adaptive += adaptiveInsulation.getInsulation();
                }
            }
            int textColor = 10526880;

            poseStack.pushPose();
            poseStack.translate(0, 0, 0);
            // Render cold insulation
            if (cold > 0)
            {   int xOffs = renderOverloadCell(graphics, font, x, y, cold, textColor, Insulation.Type.COLD);
                poseStack.translate(xOffs, 0, 0);
            }
            if (heat > 0)
            {   int xOffs = renderOverloadCell(graphics, font, x, y, heat, textColor, Insulation.Type.HEAT);
                poseStack.translate(xOffs, 0, 0);
            }
            if (neutral > 0)
            {   int xOffs = renderOverloadCell(graphics, font, x, y, neutral, textColor, Insulation.Type.NEUTRAL);
                poseStack.translate(xOffs, 0, 0);
            }
            if (adaptive > 0)
            {   int xOffs = renderOverloadCell(graphics, font, x, y, adaptive, textColor, Insulation.Type.ADAPTIVE);
                poseStack.translate(xOffs, 0, 0);
            }
            poseStack.popPose();
        }
        // Insulation is small enough to represent traditionally
        else for (Insulation insulation : sortedInsulation)
        {
            if (insulation instanceof AdaptiveInsulation adaptive)
            {
                double value = adaptive.getInsulation();

                for (int i = 0; i < CSMath.ceil(Math.abs(value)) / 2; i++)
                {
                    double insul = CSMath.minAbs(CSMath.shrink(value, i * 2), 2);
                    // adaptive cells base color
                    renderCell(graphics, x + 7, y + 1, insul, 12, true);

                    // adaptive cells overlay
                    double blend = Math.abs(adaptive.getFactor());
                    int overlayU = switch (CSMath.sign(adaptive.getFactor()))
                    {   case -1 -> 6;
                        case 1 -> 18;
                        default -> 12;
                    };
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1, 1, 1, (float) blend);
                    renderCell(graphics, x + 7, y + 1, insul, overlayU, true);
                    RenderSystem.disableBlend();
                    RenderSystem.setShaderColor(1, 1, 1, 1f);

                    poseStack.translate(6, 0, 0);
                }
            }
            else if (insulation instanceof StaticInsulation staticInsulation)
            {
                double cold = staticInsulation.getCold();
                double hot = staticInsulation.getHeat();
                double neutral = cold > 0 == hot > 0
                                 ? CSMath.minAbs(cold, hot)
                                 : 0;
                cold -= neutral;
                hot -= neutral;

                // Cold insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(cold)) / 2; i++)
                {   double coldInsul = CSMath.minAbs(CSMath.shrink(cold, i * 2), 2);
                    renderCell(graphics, x + 7, y + 1, coldInsul, 12, false); // cold cells
                    poseStack.translate(6, 0, 0);
                }

                // Neutral insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(neutral)); i++)
                {
                    double neutralInsul = CSMath.minAbs(CSMath.shrink(neutral, i), 1) * 2;
                    renderCell(graphics, x + 7, y + 1, neutralInsul, 6, false); // neutral cells
                    poseStack.translate(6, 0, 0);
                }

                // Hot insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(hot)) / 2; i++)
                {
                    double hotInsul = CSMath.minAbs(CSMath.shrink(hot, i * 2), 2);
                    renderCell(graphics, x + 7, y + 1, hotInsul, 18, false); // hot cells
                    poseStack.translate(6, 0, 0);
                }
            }
        }
        poseStack.popPose();

        // border
        for (int i = 0; i < insulSlotCount && !overflow; i++)
        {
            boolean end = i == insulSlotCount - 1;
            if (end)
            {
                graphics.blit(TOOLTIP_LOCATION.get(),
                              x + 7 + i * 6, //x
                              y, //y
                              5, //width
                              6, //height
                              6, //u
                              0, //v
                              3, //uWidth
                              6, //vHeight
                              32,
                              24);
                graphics.blit(TOOLTIP_LOCATION.get(),
                              x + 7 + i * 6 + 4, //x
                              y, //y
                              3, //width
                              6, //height
                              8, //u
                              0, //v
                              3, //uWidth
                              6, //vHeight
                              32,
                              24);
            }
            else
            {
                graphics.blit(TOOLTIP_LOCATION.get(),
                              x + 7 + i * 6, //x
                              y, //y
                              6, //width
                              6, //height
                              6, //u
                              0, //v
                              3, //uWidth
                              6, //vHeight
                              32,
                              24);
            }
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
