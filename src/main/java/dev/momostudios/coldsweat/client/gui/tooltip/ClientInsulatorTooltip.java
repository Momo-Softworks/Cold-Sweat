package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientInsulatorTooltip implements ClientTooltipComponent
{
    Pair<Double, Double> insulationValues;
    double cold = 0;
    double hot = 0;
    double neutral = 0;
    boolean isAdaptive;
    int width = 0;

    public ClientInsulatorTooltip(Pair<Double, Double> insulationValues, boolean isAdaptive)
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
    public int getWidth(Font font)
    {
        return width + 12;
    }

    public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int depth)
    {
        cold = insulationValues.getFirst();
        hot = insulationValues.getSecond();
        neutral = (cold > 0 == hot > 0 ? CSMath.minAbs(cold, hot) : 0) * 2;
        hot -= neutral/2;
        cold -= neutral/2;

        int coldSlots = Math.abs(CSMath.ceil(cold/2));
        int neutralSlots = Math.abs(CSMath.ceil(neutral/2));
        int hotSlots = Math.abs(CSMath.ceil(hot/2));
        width = coldSlots + neutralSlots + hotSlots;

        RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png"));

        // Render positive and negative insulation separately
        int barLength = 0;
        int totalPosSlots = isAdaptive ? coldSlots : CSMath.ceil(CSMath.max(coldSlots, hotSlots, neutralSlots));
        int totalNegSlots = CSMath.ceil(Math.abs(CSMath.min(CSMath.ceil(cold/2), CSMath.ceil(hot/2), CSMath.ceil(neutral/2))));

        // Positive insulation bar
        if (totalPosSlots > 0)
        {
            // background
            for (int i = 0; i < totalPosSlots; i++)
            {   GuiComponent.blit(poseStack, x + 7 + i*6, y + 1, 0, 0, 0, 6, 4, 32, 16);
            }

            if (isAdaptive)
            {   renderCells(poseStack, x + 7, y + 1, coldSlots, Math.ceil(cold), 0); // adaptive cells
            }
            else
            {
                int xOffs = 0;
                if (cold > 0)
                {   renderCells(poseStack, x + 7, y + 1, coldSlots, cold, 12); // cold cells
                    xOffs += coldSlots * 6;
                }
                if (neutral > 0)
                {   renderCells(poseStack, x + 7 + xOffs, y + 1, neutralSlots, neutral, 6); // neutral cells
                    xOffs += neutralSlots * 6;
                }
                if (hot > 0)
                {   renderCells(poseStack, x + 7 + xOffs, y + 1, hotSlots, hot, 18); // hot cells
                }
            }
            for (int i = 0; i < totalPosSlots; i++)
            {
                boolean end = i == totalPosSlots - 1;
                // border
                GuiComponent.blit(poseStack, x + 7 + i * 6, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 32, 16);
            }

            // icon
            GuiComponent.blit(poseStack, x, y - 1, 0, 24, 0, 8, 8, 32, 16);
            barLength += totalPosSlots * 6 + 12;
        }

        // Negative insulation bar
        if (totalNegSlots > 0)
        {
            // background
            for (int i = 0; i < totalNegSlots; i++)
            {   GuiComponent.blit(poseStack, x + 7 + i * 6 + barLength, y + 1, 0, 0, 0, 6, 4, 32, 16);
            }

            // slots
            int xOffs = 0;
            if (cold < 0)
            {   renderCells(poseStack, x + 7 + barLength, y + 1, coldSlots, -cold, 12); // cold cells
                xOffs += coldSlots * 6;
            }
            if (neutral < 0)
            {   renderCells(poseStack, x + 7 + barLength + xOffs, y + 1, neutralSlots, -neutral, 6); // neutral cells
                xOffs += neutralSlots * 6;
            }
            if (hot < 0)
            {   renderCells(poseStack, x + 7 + barLength + xOffs, y + 1, hotSlots, -hot, 18); // hot cells
            }

            // border
            for (int i = 0; i < totalNegSlots; i++)
            {
                boolean end = i == totalNegSlots - 1;
                GuiComponent.blit(poseStack, x + 7 + i * 6 + barLength, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 32, 16);
            }

            // icon
            GuiComponent.blit(poseStack, x + barLength, y - 1, 0, 24, 0, 8, 8, 32, 16);
            // negative sign
            GuiComponent.blit(poseStack, x + 3 + barLength, y + 3, 0, 19, 5, 5, 3, 32, 16);
            // positive sign
            GuiComponent.blit(poseStack, x + 3, y + 2, 0, 19, 0, 5, 5, 32, 16);

            barLength += totalNegSlots * 6 + 12;
        }

        width = barLength;
    }

    static void renderCells(PoseStack poseStack, int x, int y, int slots, double insulation, int uvX)
    {
        for (int i = 0; i < slots; i++)
        {
            int uvY = insulation - i * CSMath.getSign(insulation) * 2 >= 2 ? 8 : 12;
            GuiComponent.blit(poseStack, x + i*6, y, 0, uvX, uvY, 6, 4, 32, 16);
        }
    }
}
