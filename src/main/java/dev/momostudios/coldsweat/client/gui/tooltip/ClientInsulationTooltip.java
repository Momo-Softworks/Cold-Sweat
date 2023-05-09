package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientInsulationTooltip implements ClientTooltipComponent
{
    List<ItemInsulationCap.InsulationPair> insulation;
    ItemStack stack;
     int width = 0;

    public ClientInsulationTooltip(List<ItemInsulationCap.InsulationPair> insulation, ItemStack stack)
    {
        this.insulation = insulation;
        this.stack = stack;
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

        int slots = ConfigSettings.INSULATION_SLOTS.get()[3 - LivingEntity.getEquipmentSlotForItem(stack).getIndex()];
        int barLength = slots * 6 + 12;
        this.width = 0;

        int fullCellV = 8;
        int partialCellV = 12;

        List<Triplet<Double, Double, Double>> positiveInsul = new ArrayList<>();
        List<Triplet<Double, Double, Double>> negativeInsul = new ArrayList<>();

        for (ItemInsulationCap.InsulationPair value : insulation)
        {
            if (value instanceof ItemInsulationCap.Insulation insul)
            {
                double cold = insul.getCold();
                double hot = insul.getHot();
                // If both are positive or negative, add to the same list
                if (cold > 0 == hot > 0 || cold == 0 || hot == 0)
                {
                    if (cold > 0 || hot > 0)
                        positiveInsul.add(new Triplet<>(cold, hot, null));
                    else
                        negativeInsul.add(new Triplet<>(cold, hot, null));
                }
                // If one is positive and one is negative, split them into two lists
                else
                {
                    if (cold > 0)
                        positiveInsul.add(new Triplet<>(cold, 0.0, null));
                    else
                        negativeInsul.add(new Triplet<>(cold, 0.0, null));

                    if (hot > 0)
                        positiveInsul.add(new Triplet<>(0.0, hot, null));
                    else
                        negativeInsul.add(new Triplet<>(0.0, hot, null));
                }
            }
            else if (value instanceof ItemInsulationCap.AdaptiveInsulation insul)
            {
                double insulation = insul.getInsulation();
                double factor = insul.getFactor();
                double hot = CSMath.blend(0, insulation, factor, -1, 1);
                double cold = CSMath.blend(insulation, 0, factor, -1, 1);
                // If positive, add to positive list, else add to negative list
                if (insulation >= 0)
                {   positiveInsul.add(new Triplet<>(cold, hot, factor));
                }
                else
                {   negativeInsul.add(new Triplet<>(cold, hot, factor));
                }
            }
        }

        // Positive (default) insulation bar
        int posSlots = positiveInsul.size();
        if (posSlots > 0)
        {
            for (int i = 0; i < Math.max(slots, posSlots); i++)
            {
                // background
                GuiComponent.blit(poseStack, x + 7 + i*6, y + 1, 0, 0, 0, 6, 4, 32, 16);
            }

            for (int i = 0; i < posSlots; i++)
            {
                Triplet<Double, Double, Double> value = positiveInsul.get(i);
                double cold = value.getA();
                double hot = value.getB();
                Double factor = value.getC();
                boolean isAdaptive = factor != null;
                int cellU;
                int cellV;
                float alpha = 1.0f;

                if (isAdaptive)
                {
                    int tempOffset = factor >= 0 ? -4 : 0;
                    cellU = 0;
                    cellV = 8 + tempOffset;
                    GuiComponent.blit(poseStack, x + 7 + i * 6, y + 1, 0, 0, 12 + tempOffset, 6, 4, 32, 16);
                    alpha = (float) Math.abs(factor);
                    if (factor < 0) alpha = 1.0f - alpha;
                }
                else
                {
                    cellU = // neutral
                            cold == hot ? 6
                            // cold
                            : Math.abs(cold) > Math.abs(hot) ? 12
                            // hot
                            : 18;
                    // partial or full cell
                    cellV = cold + hot >= 2 ? fullCellV : partialCellV;
                }

                // cells
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
                GuiComponent.blit(poseStack, x + 7 + i * 6, y + 1, 0, cellU, cellV, 6, 4, 32, 16);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                RenderSystem.disableBlend();
            }

            // border
            for (int i = 0; i < Math.max(slots, posSlots); i++)
            {
                boolean end = i == Math.max(slots, posSlots) - 1;
                GuiComponent.blit(poseStack, x + 7 + i*6, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 32, 16);
            }
            // icon
            GuiComponent.blit(poseStack, x, y - 1, 0, 24, 8, 8, 8, 32, 16);

            this.width = barLength;
        }

        // Negative insulation bar
        int negSlots = negativeInsul.size();
        if (negSlots > 0)
        {
            for (int i = 0; i < Math.max(slots, negSlots); i++)
            {
                // background
                GuiComponent.blit(poseStack, x + 7 + i*6 + barLength, y + 1, 0, 0, 0, 6, 4, 32, 16);
            }

            for (int i = 0; i < negSlots; i++)
            {
                Triplet<Double, Double, Double> value = negativeInsul.get(i);
                double cold = value.getA();
                double hot = value.getB();
                Double factor = value.getC();
                boolean isAdaptive = factor != null;
                int cellU;
                int cellV;
                float alpha = 1.0f;

                if (isAdaptive)
                {
                    cellU = 0;
                    cellV = 4;
                    GuiComponent.blit(poseStack, x + 7 + i * 6, y + 1, 0, 0, 12, 6, 4, 32, 16);
                    alpha = (float) CSMath.blend(1, 0, factor, -1, 1);
                }
                else
                {
                    cellU = // neutral
                            cold == hot ? 6
                            // cold
                            : Math.abs(cold) > Math.abs(hot) ? 12
                            // hot
                            : 18;
                    // partial or full cell
                    cellV = cold + hot >= 2 ? fullCellV : partialCellV;
                }

                // cells
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
                GuiComponent.blit(poseStack, x + 7 + i * 6 + barLength, y + 1, 0, cellU, cellV, 6, 4, 32, 16);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                RenderSystem.disableBlend();
            }

            // border
            for (int i = 0; i < Math.max(slots, negSlots); i++)
            {
                boolean end = i == Math.max(slots, negSlots) - 1;
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
