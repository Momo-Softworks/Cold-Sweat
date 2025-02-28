package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientInsulationTooltip extends Tooltip
{
    private static final ResourceLocation TOOLTIP = new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png");
    private static final ResourceLocation TOOLTIP_HC = new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar_hc.png");
    public static final Supplier<ResourceLocation> TOOLTIP_LOCATION = () -> ConfigSettings.HIGH_CONTRAST.get()
                                                                            ? TOOLTIP_HC
                                                                            : TOOLTIP;
    private static final Minecraft MC = Minecraft.getInstance();

    List<Insulation> insulation;
    Insulation.Slot slot;
    int width = 0;
    ItemStack stack;
    boolean strikethrough;

    private static final Method INNER_BLIT = ObfuscationReflectionHelper.findMethod(AbstractGui.class, "func_238469_a_",
                                                                                    MatrixStack.class, int.class, int.class, int.class,
                                                                                    int.class, int.class, int.class, int.class,
                                                                                    float.class, float.class, int.class, int.class);
    static
    {   INNER_BLIT.setAccessible(true);
    }

    public static void innerBlit(MatrixStack poseStack, int x1, int x2, int y1, int y2, int zOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight)
    {   try
    {   INNER_BLIT.invoke(null, poseStack, x1, x2, y1, y2, zOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }
    catch (Exception e)
    {   e.printStackTrace();
    }
    }
    public static void blit(MatrixStack poseStack, int x, int y, int zOffset, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight)
    {   innerBlit(poseStack, x, x + width, y, y + height, zOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }


    public ClientInsulationTooltip(List<Insulation> insulation, Insulation.Slot slot, ItemStack stack, boolean strikethrough)
    {   this.insulation = insulation;
        this.slot = slot;
        this.stack = stack;
        this.strikethrough = strikethrough;
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
    public void renderImage(FontRenderer font, int x, int y, MatrixStack poseStack, ItemRenderer itemRenderer, int depth)
    {
        y += 2;
        Minecraft.getInstance().textureManager.bind(TOOLTIP_LOCATION.get());

        List<Insulation> posInsulation = new ArrayList<>();
        List<Insulation> negInsulation = new ArrayList<>();

        // Separate insulation into negative & positive
        for (Insulation ins : insulation)
        {
            if (ins instanceof StaticInsulation)
            {
                StaticInsulation insul = (StaticInsulation) ins;
                double cold = insul.getCold();
                double hot = insul.getHeat();

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
        poseStack.pushPose();
        width = 0;

        // Positive insulation bar
        if (!posInsulation.isEmpty())
        {
            BarType barType = negInsulation.isEmpty() ? BarType.NONE : BarType.POSITIVE;
            width += renderBar(poseStack, x + width, y, posInsulation, slot, stack, barType);
        }
        // Negative insulation bar
        if (!negInsulation.isEmpty())
        {
            if (!posInsulation.isEmpty()) width += 4;
            width += renderBar(poseStack, x + width, y, negInsulation, slot, stack, BarType.NEGATIVE);
        }
        poseStack.popPose();
        // Render strikethrough
        if (this.strikethrough)
        {
            AbstractGui.fill(poseStack, x - 1, y + 2, x + 8, y + 3, 0xFFF63232);
            AbstractGui.fill(poseStack, x, y + 3, x + 9, y + 4, 0xFFF63232);
        }
    }

    static boolean RECURSIVE = false;
    static void renderCell(MatrixStack poseStack, int x, int y, Insulation insulation)
    {
        Minecraft.getInstance().textureManager.bind(TOOLTIP_LOCATION.get());
        double rounded = CSMath.roundNearest(Math.abs(insulation.getValue()), 0.25);
        // Determine cell temperature
        int uvX = 0;
        if (insulation instanceof AdaptiveInsulation)
        {
            AdaptiveInsulation adaptive = (AdaptiveInsulation) insulation;
            if (!RECURSIVE) uvX = 16;
            else uvX = adaptive.getFactor() < 0 ? 10
                     : adaptive.getFactor() == 0 ? 16
                     : 22;
        }
        else if (insulation instanceof StaticInsulation)
        {
            StaticInsulation stat = (StaticInsulation) insulation;
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
        renderCellBackground(poseStack, x, y);
        // Render base cell
        MC.textureManager.bind(TOOLTIP_LOCATION.get());
        Screen.blit(poseStack, x, y, 0, uvX, uvY, 6, 4, 28, 36);
        // Render color overlay for adaptive insulation
        if (insulation instanceof AdaptiveInsulation && ((AdaptiveInsulation) insulation).getFactor() != 0 && !RECURSIVE)
        {
            AdaptiveInsulation adaptive = (AdaptiveInsulation) insulation;
            double blend = Math.abs(adaptive.getFactor());
            RenderSystem.enableBlend();
            GlStateManager._color4f(1, 1, 1, (float) blend);
            RECURSIVE = true;
            renderCell(poseStack, x, y, insulation);
            RenderSystem.disableBlend();
            GlStateManager._color4f(1, 1, 1, 1f);
        }
        RECURSIVE = false;
    }

    static void renderCellBackground(MatrixStack poseStack, int x, int y)
    {
        MC.textureManager.bind(TOOLTIP_LOCATION.get());
        // Render background
        Screen.blit(poseStack, x, y, 0, 0, 0, 6, 4, 28, 36);
    }

    static void renderIcon(MatrixStack poseStack, int x, int y, Insulation.Slot slot, BarType type)
    {
        MC.textureManager.bind(TOOLTIP_LOCATION.get());
        // icon
        switch (slot)
        {
            case ITEM  : Screen.blit(poseStack, x, y, 0, 28, 0,  8, 8, 28, 36); break;
            case ARMOR : Screen.blit(poseStack, x, y, 0, 28, 8,  8, 8, 28, 36); break;
            case CURIO : Screen.blit(poseStack, x, y, 0, 28, 16, 8, 8, 28, 36); break;
        }
        // positive/negative sign
        switch (type)
        {
            case POSITIVE : Screen.blit(poseStack, x + 3, y + 3, 0, 18, 0, 5, 5, 28, 36); break;
            case NEGATIVE : Screen.blit(poseStack, x + 3, y + 3, 0, 23, 0, 5, 5, 28, 36); break;
        }
    }

    static int renderBar(MatrixStack poseStack, int x, int y, List<Insulation> insulations, Insulation.Slot slot, ItemStack stack, BarType type)
    {
        MC.textureManager.bind(TOOLTIP_LOCATION.get());
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
        {   finalWidth = renderOverflowBar(poseStack, x + 8, y, sortedInsulation, slots);
        }
        else if (mode == Mode.COMPOUND)
        {   finalWidth = renderCompoundBar(poseStack, x + 7, y, sortedInsulation, slots);
        }
        else
        {   finalWidth = renderNormalBar(poseStack, x + 7, y, sortedInsulation, slots);
        }
        poseStack.popPose();
        renderIcon(poseStack, x, y, slot, type);
        // Return the width of the tooltip
        if (mode != Mode.OVERFLOW) finalWidth += 2;
        return finalWidth + 6;
    }

    static int renderNormalBar(MatrixStack poseStack, int x, int y, List<Insulation> insulations, int slots)
    {
        // Cells
        for (int i = 0; i < insulations.size(); i++)
        {
            Insulation insulation = insulations.get(i);
            renderCell(poseStack, x + i*6, y + 2, insulation);
        }
        // Remaining background
        for (int i = insulations.size(); i < slots; i++)
        {   renderCellBackground(poseStack, x + i*6, y + 2);
        }
        // Border
        for (int i = 0; i < slots; i++)
        {
            BorderSegment segment = getBorderSegment(slots, i);
            if (segment == BorderSegment.SINGLE) segment = BorderSegment.TAIL;
            if (segment == BorderSegment.HEAD) segment = BorderSegment.BODY;
            renderCellBorder(poseStack, x + i*6, y + 2, segment, BorderType.NORMAL);
        }
        return Math.max(insulations.size(), slots) * 6;
    }

    static int renderCompoundBar(MatrixStack poseStack, int x, int y, List<Insulation> insulations, int slots)
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
                    renderCell(poseStack, x + cellX, y + 2, subInsul);
                    renderCellBorder(poseStack, x + cellX, y + 2, segment, BorderType.COMPOUND);
                    // Increment cell position
                    cellX += 6;
                }
                // Render divider
                if (i < slots - 1)
                {   cellX += 1;
                    if (i < insulations.size() - 1 && insulations.get(i + 1).split().size() > 1)
                    {   renderCellBorder(poseStack, x + cellX, y + 2, BorderSegment.TAIL, BorderType.DIVIDER);
                    }
                    cellX += 1;
                }
            }
            else // Insulation is small enough to represent traditionally
            {
                // Count normal insulations. The first normal insulation should have a head border
                int normalCount = insulations.size() - compoundCount;
                renderCell(poseStack, x + cellX, y + 2, insulation);
                // Render cell border
                BorderSegment segment = getBorderSegment(normalCount, i - compoundCount);
                if (segment == BorderSegment.TAIL && i < slots - 1) segment = BorderSegment.BODY;
                BorderType borderType = segment == BorderSegment.TAIL ? BorderType.NORMAL : BorderType.SEGMENT;
                renderCellBorder(poseStack, x + cellX, y + 2, segment, borderType);
                cellX += 6;
                // Render divider
                if (i < slots - 1)
                {   renderCellBorder(poseStack, x + cellX, y + 2, BorderSegment.BODY, BorderType.DIVIDER);
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
            renderCellBackground(poseStack, x + cellX, y + 2);
            // Render border
            renderCellBorder(poseStack, x + cellX, y + 2, segment, borderType);
            cellX += 6;
            // Render divider
            if (i < emptySlots - 1)
            {   renderCellBorder(poseStack, x + cellX, y + 2, BorderSegment.TAIL, BorderType.EMPTY_DIVIDER);
                cellX += 1;
            }
        }
        return cellX;
    }

    static int renderOverflowBar(MatrixStack poseStack, int x, int y, List<Insulation> insulations, int slots)
    {
        int width = 0;
        FontRenderer font = Minecraft.getInstance().font;
        // tally up the insulation from the sorted list into cold, hot, neutral, and adaptive
        double cold = 0;
        double heat = 0;
        double neutral = 0;
        double adaptive = 0;

        for (Insulation insulation : insulations)
        {
            if (insulation instanceof StaticInsulation)
            {
                StaticInsulation staticInsulation = (StaticInsulation) insulation;
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
            else if (insulation instanceof AdaptiveInsulation)
            {
                AdaptiveInsulation adaptiveInsulation = (AdaptiveInsulation) insulation;
                double thisAdaptive = Math.abs(adaptiveInsulation.getInsulation());
                adaptive += thisAdaptive;
            }
        }
        int textColor = 10526880;

        poseStack.pushPose();
        if (insulations.size() < slots)
        {
            int xOffs = renderEmptyBar(poseStack, x, y + 2, slots - insulations.size());
            width += xOffs;
            poseStack.translate(xOffs, 0, 0);
        }
        if (cold > 0)
        {
            int xOffs = renderOverflowCell(poseStack, font, x + 1, y + 2, new StaticInsulation(cold, 0), textColor);
            width += xOffs;
            poseStack.translate(xOffs, 0, 0);
        }
        if (heat > 0)
        {
            int xOffs = renderOverflowCell(poseStack, font, x + 1, y + 2, new StaticInsulation(0, heat), textColor);
            width += xOffs;
            poseStack.translate(xOffs, 0, 0);
        }
        if (neutral > 0)
        {
            int xOffs = renderOverflowCell(poseStack, font, x + 1, y + 2, new StaticInsulation(neutral, neutral), textColor);
            width += xOffs;
            poseStack.translate(xOffs, 0, 0);
        }
        if (adaptive > 0)
        {
            int xOffs = renderOverflowCell(poseStack, font, x + 1, y + 2, new AdaptiveInsulation(adaptive, 0), textColor);
            width += xOffs;
            poseStack.translate(xOffs, 0, 0);
        }
        poseStack.popPose();
        return width;
    }
    static int renderOverflowCell(MatrixStack poseStack, FontRenderer font, int x, int y, Insulation insulation, int textColor)
    {
        Number insul = CSMath.truncate(insulation.getValue() / 2, 2);
        if (CSMath.isInteger(insul)) insul = insul.intValue();
        String text = "x" + insul;

        renderCell(poseStack, x, y, insulation);
        renderCellBorder(poseStack, x, y, BorderSegment.HEAD, BorderType.OVERFLOW);
        renderCellBorder(poseStack, x, y, BorderSegment.BODY, BorderType.OVERFLOW);
        renderCellBorder(poseStack, x, y, BorderSegment.TAIL, BorderType.OVERFLOW);
        Screen.drawString(poseStack, font, text, x + 8, y - 2, textColor);
        // Return the width of the cell and text
        return 12 + font.width(text);
    }
    static int renderEmptyBar(MatrixStack poseStack, int x, int y, int size)
    {
        MC.textureManager.bind(TOOLTIP_LOCATION.get());
        for (int i = 0; i < size; i++)
        {
            BorderSegment segment = getBorderSegment(size, i);
            // background
            Screen.blit(poseStack, x + 7 + i * 6, y + 1, 0, 0, 0, 6, 4, 28, 36);
            // border
            renderCellBorder(poseStack, x + i * 6, y, segment, BorderType.OVERFLOW);
        }
        poseStack.pushPose();
        poseStack.popPose();
        return size * 6 + 4;
    }

    static void renderCellBorder(MatrixStack poseStack, int x, int y, BorderSegment segment, BorderType type)
    {
        MC.textureManager.bind(TOOLTIP_LOCATION.get());
        switch (type)
        {
            case DIVIDER : Screen.blit(poseStack, x, y - 1, 0, 10, 0, 1, 6, 28, 36); break;
            case EMPTY_DIVIDER : Screen.blit(poseStack, x, y - 1, 0, 11, 0, 1, 6, 28, 36); break;
            default :
            {
                int vOffset;
                switch (type)
                {   case NORMAL : vOffset = 4; break;
                    case OVERFLOW : vOffset = 10; break;
                    case SEGMENT : vOffset = 16; break;
                    case COMPOUND : vOffset = 22; break;
                    default : vOffset = 0;
                }
                switch (segment)
                {
                    case SINGLE:
                        if (!RECURSIVE)
                        {
                            RECURSIVE = true;
                            renderCellBorder(poseStack, x, y, BorderSegment.HEAD, type);
                            renderCellBorder(poseStack, x, y, BorderSegment.TAIL, type);
                            renderCellBorder(poseStack, x, y, BorderSegment.BODY, type);
                            RECURSIVE = false;
                        }
                        break;
                    case HEAD:
                        Screen.blit(poseStack, x - 1, y - 1, 0, 0, vOffset, 7, 6, 28, 36);
                        break;
                    case BODY:
                        Screen.blit(poseStack, x + 0, y - 1, 0, 2, vOffset, 6, 6, 28, 36);
                        break;
                    case TAIL:
                        Screen.blit(poseStack, x + 0, y - 1, 0, 3, vOffset, 7, 6, 28, 36);
                        break;
                }
                break;
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
            if (insul instanceof AdaptiveInsulation)
            {
                // Set factor stored in NBT
                AdaptiveInsulation.setFactorFromNBT(((AdaptiveInsulation) insul), stack);
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

