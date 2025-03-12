package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    Insulation.Slot slot;
    int width = 0;
    ItemStack stack;
    boolean strikethrough;

    public ClientInsulationTooltip(List<Insulation> insulation, Insulation.Slot slot, ItemStack stack, boolean strikethrough)
    {
        if (slot != Insulation.Slot.ITEM)
        {   insulation = Insulation.splitList(insulation);
        }
        this.insulation = insulation;
        this.slot = slot;
        this.stack = stack;
        this.strikethrough = strikethrough;
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

        // Separate insulation into negative & positive
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
        {
            BarType barType = negInsulation.isEmpty() ? BarType.NONE : BarType.POSITIVE;
            width += renderBar(graphics, x + width, y, posInsulation, slot, stack, barType);
        }
        // Negative insulation bar
        if (!negInsulation.isEmpty())
        {
            if (!posInsulation.isEmpty()) width += 4;
            width += renderBar(graphics, x + width, y, negInsulation, slot, stack, BarType.NEGATIVE);
        }
        poseStack.popPose();
        // Render strikethrough
        if (this.strikethrough)
        {
            graphics.fill(x - 1, y + 2, x + 8, y + 3, 0xFFF63232);
            graphics.fill(x, y + 3, x + 9, y + 4, 0xFFF63232);
        }
    }

    static boolean RECURSIVE = false;
    static void renderCell(GuiGraphics graphics, int x, int y, Insulation insulation)
    {
        double rounded = CSMath.roundNearest(Math.abs(insulation.getValue()), 0.25);
        // Determine cell temperature
        int uvX = 0;
        if (insulation instanceof AdaptiveInsulation adaptive)
        {
            if (!RECURSIVE) uvX = 16;
            else uvX = adaptive.getFactor() < 0 ? 10
                     : adaptive.getFactor() == 0 ? 16
                     : 22;
        }
        else if (insulation instanceof StaticInsulation stat)
        {
            double cold = Math.abs(stat.getCold());
            double heat = Math.abs(stat.getHeat());
            uvX = cold > heat ? 10
                : cold == heat ? 16
                : 22;
        }

        // Determining rendering full or partial cell
        int uvY = insulation instanceof AdaptiveInsulation
                  ? (rounded >= 2 ? 16 : 20)
                  : (rounded >= 2 ? 8 : 12);
        // Render background
        renderCellBackground(graphics, x, y);
        // Render base cell
        graphics.blit(TOOLTIP_LOCATION.get(), x, y, 0, uvX, uvY, 6, 4, 36, 28);
        // Render color overlay for adaptive insulation
        if (insulation instanceof AdaptiveInsulation adaptive && adaptive.getFactor() != 0 && !RECURSIVE)
        {
            double blend = Math.abs(adaptive.getFactor());
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, (float) blend);
            RECURSIVE = true;
            renderCell(graphics, x, y, insulation);
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1f);
        }
        RECURSIVE = false;
    }

    static void renderCellBackground(GuiGraphics graphics, int x, int y)
    {
        // Render background
        graphics.blit(TOOLTIP_LOCATION.get(), x, y, 0, 0, 0, 6, 4, 36, 28);
    }

    static void renderIcon(GuiGraphics graphics, int x, int y, Insulation.Slot slot, BarType type)
    {
        // icon
        switch (slot)
        {
            case ITEM ->  graphics.blit(TOOLTIP_LOCATION.get(), x, y, 0, 28, 0,  8, 8, 36, 28);
            case ARMOR -> graphics.blit(TOOLTIP_LOCATION.get(), x, y, 0, 28, 8,  8, 8, 36, 28);
            case CURIO -> graphics.blit(TOOLTIP_LOCATION.get(), x, y, 0, 28, 16, 8, 8, 36, 28);
        }
        // positive/negative sign
        switch (type)
        {
            case POSITIVE -> graphics.blit(TOOLTIP_LOCATION.get(), x + 3, y + 3, 0, 18, 0, 5, 5, 36, 28);
            case NEGATIVE -> graphics.blit(TOOLTIP_LOCATION.get(), x + 3, y + 3, 0, 23, 0, 5, 5, 36, 28);
        }
    }

    static int renderBar(GuiGraphics graphics, int x, int y, List<Insulation> insulations, Insulation.Slot slot, ItemStack stack, BarType type)
    {
        PoseStack poseStack = graphics.pose();
        List<Insulation> sortedInsulation = Insulation.sort(insulations);
        setAdaptations(sortedInsulation, stack);

        Mode mode;
        if (sortedInsulation.stream().map(Insulation::split).mapToInt(List::size).sum() > 10)
        {   mode = Mode.OVERFLOW;
        }
        else if (insulations.stream().anyMatch(insul -> insul.split().size() > 1))
        {   mode = Mode.COMPOUND;
        }
        else mode = Mode.NORMAL;

        int armorSlots = slot == Insulation.Slot.ARMOR && type != BarType.NEGATIVE && ItemInsulationManager.isInsulatable(stack)
                         ? ItemInsulationManager.getInsulationSlots(stack)
                         : 0;
        int slots = Math.max(armorSlots, insulations.size());

        /* Insulation */
        poseStack.pushPose();
        int finalWidth;
        if (mode == Mode.OVERFLOW)
        {   finalWidth = renderOverflowBar(graphics, x + 8, y, sortedInsulation, slots);
        }
        else if (mode == Mode.COMPOUND)
        {   finalWidth = renderCompoundBar(graphics, x + 7, y, sortedInsulation, slots);
        }
        else
        {   finalWidth = renderNormalBar(graphics, x + 7, y, sortedInsulation, slots);
        }
        poseStack.popPose();
        renderIcon(graphics, x, y, slot, type);
        // Return the width of the tooltip
        if (mode != Mode.OVERFLOW) finalWidth += 2;
        return finalWidth + 6;
    }

    static int renderNormalBar(GuiGraphics graphics, int x, int y, List<Insulation> insulations, int slots)
    {
        // Cells
        for (int i = 0; i < insulations.size(); i++)
        {
            Insulation insulation = insulations.get(i);
            renderCell(graphics, x + i*6, y + 2, insulation);
        }
        // Remaining background
        for (int i = insulations.size(); i < slots; i++)
        {   renderCellBackground(graphics, x + i*6, y + 2);
        }
        // Border
        for (int i = 0; i < slots; i++)
        {
            BorderSegment segment = getBorderSegment(slots, i);
            if (segment == BorderSegment.SINGLE) segment = BorderSegment.TAIL;
            if (segment == BorderSegment.HEAD) segment = BorderSegment.BODY;
            renderCellBorder(graphics, x + i*6, y + 2, segment, BorderType.NORMAL);
        }
        return Math.max(insulations.size(), slots) * 6;
    }

    static int renderCompoundBar(GuiGraphics graphics, int x, int y, List<Insulation> insulations, int slots)
    {
        int cellX = 0;
        int compoundCount = 0;
        for (int i = 0; i < insulations.size(); i++)
        {
            Insulation insulation = insulations.get(i);
            // Split insulation into segments (if possible)
            List<Insulation> subInsulations = Insulation.sort(insulation.split());

            // Insulation is large enough to split into segments
            if (subInsulations.size() > 1)
            {
                compoundCount++;
                // Render sub-insulation cells
                for (int j = 0; j < subInsulations.size(); j++)
                {
                    Insulation subInsul = subInsulations.get(j);
                    BorderSegment segment = getBorderSegment(subInsulations.size(), j);
                    // Render cell
                    renderCell(graphics, x + cellX, y + 2, subInsul);
                    renderCellBorder(graphics, x + cellX, y + 2, segment, BorderType.COMPOUND);
                    // Increment cell position
                    cellX += 6;
                }
                // Render divider
                if (i < slots - 1)
                {   cellX += 1;
                    if (i < insulations.size() - 1 && insulations.get(i + 1).split().size() > 1)
                    {   renderCellBorder(graphics, x + cellX, y + 2, BorderSegment.TAIL, BorderType.DIVIDER);
                    }
                    cellX += 1;
                }
            }
            else // Insulation is small enough to represent traditionally
            {
                // Count normal insulations. The first normal insulation should have a head border
                int normalCount = insulations.size() - compoundCount;
                renderCell(graphics, x + cellX, y + 2, insulation);
                // Render cell border
                BorderSegment segment = getBorderSegment(normalCount, i - compoundCount);
                if (segment == BorderSegment.TAIL && i < slots - 1) segment = BorderSegment.BODY;
                BorderType borderType = segment == BorderSegment.TAIL ? BorderType.NORMAL : BorderType.SEGMENT;
                renderCellBorder(graphics, x + cellX, y + 2, segment, borderType);
                cellX += 6;
                // Render divider
                if (i < slots - 1)
                {   renderCellBorder(graphics, x + cellX, y + 2, BorderSegment.BODY, BorderType.DIVIDER);
                    cellX += 1;
                }
            }
        }
        // Render empty cells
        int emptySlots = slots - insulations.size();
        for (int i = 0; i < emptySlots; i++)
        {
            // Render background
            BorderSegment segment = getBorderSegment(emptySlots, i);
            if (segment == BorderSegment.SINGLE) segment = BorderSegment.TAIL;
            BorderType borderType = segment == BorderSegment.TAIL ? BorderType.NORMAL : BorderType.SEGMENT;
            renderCellBackground(graphics, x + cellX, y + 2);
            // Render border
            renderCellBorder(graphics, x + cellX, y + 2, segment, borderType);
            cellX += 6;
            // Render divider
            if (i < emptySlots - 1)
            {   renderCellBorder(graphics, x + cellX, y + 2, BorderSegment.TAIL, BorderType.EMPTY_DIVIDER);
                cellX += 1;
            }
        }
        return cellX;
    }

    static int renderOverflowBar(GuiGraphics graphics, int x, int y, List<Insulation> insulations, int slots)
    {
        int width = 0;
        PoseStack poseStack = graphics.pose();
        Font font = Minecraft.getInstance().font;
        // tally up the insulation from the sorted list into cold, hot, neutral, and adaptive
        double cold = 0;
        double heat = 0;
        double neutral = 0;
        double adaptive = 0;

        for (Insulation insulation : insulations)
        {
            if (insulation instanceof StaticInsulation staticInsulation)
            {
                double thisCold = Math.abs(staticInsulation.getCold());
                double thisHeat = Math.abs(staticInsulation.getHeat());
                if (thisCold == thisHeat)
                {   neutral += thisCold;
                }
                else
                {   cold += thisCold;
                    heat += thisHeat;
                }
            }
            else if (insulation instanceof AdaptiveInsulation adaptiveInsulation)
            {
                double thisAdaptive = Math.abs(adaptiveInsulation.getInsulation());
                adaptive += thisAdaptive;
            }
        }
        int textColor = 10526880;

        poseStack.pushPose();
        if (insulations.size() < slots)
        {
            int xOffs = renderEmptyBar(graphics, x, y + 2, slots - insulations.size());
            width += xOffs;
            poseStack.translate(xOffs, 0, 0);
        }
        if (cold > 0)
        {
            int xOffs = renderOverflowCell(graphics, font, x + 1, y + 2, new StaticInsulation(cold, 0), textColor);
            width += xOffs;
            poseStack.translate(xOffs, 0, 0);
        }
        if (heat > 0)
        {
            int xOffs = renderOverflowCell(graphics, font, x + 1, y + 2, new StaticInsulation(0, heat), textColor);
            width += xOffs;
            poseStack.translate(xOffs, 0, 0);
        }
        if (neutral > 0)
        {
            int xOffs = renderOverflowCell(graphics, font, x + 1, y + 2, new StaticInsulation(neutral, neutral), textColor);
            width += xOffs;
            poseStack.translate(xOffs, 0, 0);
        }
        if (adaptive > 0)
        {
            int xOffs = renderOverflowCell(graphics, font, x + 1, y + 2, new AdaptiveInsulation(adaptive, 0), textColor);
            width += xOffs;
            poseStack.translate(xOffs, 0, 0);
        }
        poseStack.popPose();
        return width;
    }
    static int renderOverflowCell(GuiGraphics graphics, Font font, int x, int y, Insulation insulation, int textColor)
    {
        Number insul = CSMath.truncate(insulation.getValue() / 2, 2);
        if (CSMath.isInteger(insul)) insul = insul.intValue();
        String text = "x" + insul;

        renderCell(graphics, x, y, insulation);
        renderCellBorder(graphics, x, y, BorderSegment.HEAD, BorderType.OVERFLOW);
        renderCellBorder(graphics, x, y, BorderSegment.BODY, BorderType.OVERFLOW);
        renderCellBorder(graphics, x, y, BorderSegment.TAIL, BorderType.OVERFLOW);
        graphics.drawString(font, text, x + 8, y - 2, textColor);
        // Return the width of the cell and text
        return 12 + font.width(text);
    }
    static int renderEmptyBar(GuiGraphics graphics, int x, int y, int size)
    {
        PoseStack poseStack = graphics.pose();
        for (int i = 0; i < size; i++)
        {
            BorderSegment segment = getBorderSegment(size, i);
            // background
            graphics.blit(TOOLTIP_LOCATION.get(), x + 7 + i * 6, y + 1, 0, 0, 0, 6, 4, 36, 28);
            // border
            renderCellBorder(graphics, x + i * 6, y, segment, BorderType.OVERFLOW);
        }
        poseStack.pushPose();
        poseStack.popPose();
        return size * 6 + 4;
    }

    static void renderCellBorder(GuiGraphics graphics, int x, int y, BorderSegment segment, BorderType type)
    {
        switch (type)
        {
            case DIVIDER -> graphics.blit(TOOLTIP_LOCATION.get(), x, y - 1, 0, 10, 0, 1, 6, 36, 28);
            case EMPTY_DIVIDER -> graphics.blit(TOOLTIP_LOCATION.get(), x, y - 1, 0, 11, 0, 1, 6, 36, 28);
            default ->
            {
                int vOffset = switch (type)
                {   case NORMAL -> 4;
                    case OVERFLOW -> 10;
                    case SEGMENT -> 16;
                    case COMPOUND -> 22;
                    default -> 0;
                };
                switch (segment)
                {
                    case SINGLE ->
                    {
                        if (!RECURSIVE)
                        {
                            RECURSIVE = true;
                            renderCellBorder(graphics, x, y, BorderSegment.HEAD, type);
                            renderCellBorder(graphics, x, y, BorderSegment.TAIL, type);
                            renderCellBorder(graphics, x, y, BorderSegment.BODY, type);
                            RECURSIVE = false;
                        }
                    }
                    case HEAD -> graphics.blit(TOOLTIP_LOCATION.get(), x - 1, y - 1, 0, 0, vOffset, 7, 6, 36, 28);
                    case BODY -> graphics.blit(TOOLTIP_LOCATION.get(), x + 0, y - 1, 0, 2, vOffset, 6, 6, 36, 28);
                    case TAIL -> graphics.blit(TOOLTIP_LOCATION.get(), x + 0, y - 1, 0, 3, vOffset, 7, 6, 36, 28);
                }
            }
        }
    }

    static BorderSegment getBorderSegment(int collectionSize, int index)
    {
        if (collectionSize == 1) return BorderSegment.SINGLE;
        if (index == collectionSize - 1) return BorderSegment.TAIL;
        if (index == 0) return BorderSegment.HEAD;
        return BorderSegment.BODY;
    }

    static void setAdaptations(List<Insulation> insulations, ItemStack stack)
    {
        for (int i = 0; i < insulations.size(); i++)
        {
            Insulation insul = insulations.get(i).copy();
            if (insul instanceof AdaptiveInsulation adaptive)
            {
                // Set factor stored in NBT
                AdaptiveInsulation.readFactorFromArmor(adaptive, stack);
            }
            insulations.set(i, insul);
        }
    }

    private enum Mode
    {   NORMAL, COMPOUND, OVERFLOW
    }

    private enum BorderSegment
    {   HEAD, BODY, TAIL, SINGLE
    }

    private enum BorderType
    {   NORMAL, OVERFLOW, SEGMENT, COMPOUND, DIVIDER, EMPTY_DIVIDER
    }

    private enum BarType
    {   POSITIVE, NEGATIVE, NONE
    }
}
