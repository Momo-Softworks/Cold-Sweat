package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.common.event.ArmorInsulation;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientInsulationTooltip implements ClientTooltipComponent
{
    List<Pair<Double, Double>> insulationValues;
    ItemStack stack;
    int size;
    int width = 0;

    public ClientInsulationTooltip(List<Pair<Double, Double>> insulationValues, ItemStack stack, int size)
    {
        this.insulationValues = insulationValues;
        this.stack = stack;
        this.size = size;
    }

    @Override
    public int getHeight()
    {
        return 10;
    }

    @Override
    public int getWidth(Font font)
    {
        return width;
    }

    public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int depth)
    {
        RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png"));

        int slots = this.size;
        int barLength = slots * 6 + 12;
        this.width = 0;

        int fullCell = 8;
        int partialCell = 12;

        List<Pair<Double, Double>> positiveInsul = new ArrayList<>();
        List<Pair<Double, Double>> negativeInsul = new ArrayList<>();
        for (Pair<Double, Double> value : insulationValues)
        {
            double cold = value.getFirst();
            double hot = value.getSecond();
            // If both are positive or negative, add to the same list
            if (cold > 0 == hot > 0 || cold == 0 || hot == 0)
            {
                if (cold > 0 || hot > 0)
                    positiveInsul.add(value);
                else
                    negativeInsul.add(value);
            }
            // If one is positive and one is negative, split them into two lists
            else
            {
                if (cold > 0)
                    positiveInsul.add(Pair.of(cold, 0.0));
                else
                    negativeInsul.add(Pair.of(cold, 0.0));

                if (hot > 0)
                    positiveInsul.add(Pair.of(0.0, hot));
                else
                    negativeInsul.add(Pair.of(0.0, hot));
            }
        }

        // Positive (default) insulation bar
        if (positiveInsul.size() > 0)
        {
            for (int i = 0; i < slots; i++)
            {
                // background
                GuiComponent.blit(poseStack, x + 7 + i*6, y + 1, 0, 0, 1, 6, 4, 32, 16);
            }

            for (int i = 0; i < positiveInsul.size(); i++)
            {
                Pair<Double, Double> value = positiveInsul.get(i);
                double cold = value.getFirst();
                double hot = value.getSecond();
                int uvX = cold == hot ? 0
                        : Math.abs(cold) > Math.abs(hot) ? 6
                        : 12;
                int uvY = cold + hot >= 2 ? fullCell : partialCell;

                // cells
                GuiComponent.blit(poseStack, x + 7 + i * 6, y + 1, 0, uvX, uvY, 6, 4, 32, 16);
            }

            for (int i = 0; i < slots; i++)
            {
                boolean end = i == slots - 1;
                // border
                GuiComponent.blit(poseStack, x + 7 + i*6, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 32, 16);
            }
            // icon
            GuiComponent.blit(poseStack, x, y - 1, 0, 24, 8, 8, 8, 32, 16);

            this.width = barLength;
        }

        // Negative insulation bar
        if (negativeInsul.size() > 0)
        {
            for (int i = 0; i < slots; i++)
            {
                // background
                GuiComponent.blit(poseStack, x + 7 + i*6 + barLength, y + 1, 0, 0, 1, 6, 4, 32, 16);
            }

            for (int i = 0; i < negativeInsul.size(); i++)
            {
                Pair<Double, Double> value = negativeInsul.get(i);
                double cold = value.getFirst();
                double hot = value.getSecond();
                int uvX = cold == hot ? 0
                        : Math.abs(cold) > Math.abs(hot) ? 6
                        : 12;
                int uvY = Math.abs(cold + hot) >= 2 ? fullCell : partialCell;

                // cells
                GuiComponent.blit(poseStack, x + 7 + i * 6 + barLength, y + 1, 0, uvX, uvY, 6, 4, 32, 16);
            }

            for (int i = 0; i < slots; i++)
            {
                boolean end = i == slots - 1;
                // border
                GuiComponent.blit(poseStack, x + 7 + i*6 + barLength, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 32, 16);
            }
            // icon
            GuiComponent.blit(poseStack, x + barLength, y - 1, 0, 24, 8, 8, 8, 32, 16);
            // positive sign
            GuiComponent.blit(poseStack, x + 3, y + 2, 0, 19, 0, 5, 5, 32, 16);
            // negative sign
            GuiComponent.blit(poseStack, x + 3 + barLength, y + 3, 0, 19, 5, 5, 3, 32, 16);

            this.width += barLength;
        }
    }
}
