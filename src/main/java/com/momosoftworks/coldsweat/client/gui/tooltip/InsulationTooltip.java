package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationCap;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.Triplet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
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

    List<ItemInsulationCap.InsulationPair> insulation;
    ItemStack stack;
     int width = 0;

    public InsulationTooltip(List<ItemInsulationCap.InsulationPair> insulation, ItemStack stack)
    {
        this.insulation = insulation;
        this.stack = stack;
    }

    @Override
    public int getHeight()
    {   return 10;
    }

    @Override
    public int getWidth(FontRenderer font)
    {   return ConfigSettings.INSULATION_SLOTS.get()[3 - MobEntity.getEquipmentSlotForItem(stack).getIndex()] * 6 + 8;
    }

    @Override
    public void renderImage(FontRenderer font, int x, int y, MatrixStack matrixStack, ItemRenderer itemRenderer, int depth)
    {
        y += 12;
        Minecraft.getInstance().textureManager.bind(TOOLTIP_LOCATION.get());

        int slots = ConfigSettings.INSULATION_SLOTS.get()[3 - MobEntity.getEquipmentSlotForItem(stack).getIndex()];

        List<Triplet<Double, Double, Double>> positiveInsul = new ArrayList<>();
        List<Triplet<Double, Double, Double>> negativeInsul = new ArrayList<>();

        for (ItemInsulationCap.InsulationPair value : insulation)
        {
            if (value instanceof ItemInsulationCap.Insulation)
            {
                ItemInsulationCap.Insulation insul = (ItemInsulationCap.Insulation) value;
                double cold = insul.getCold();
                double hot = insul.getHot();
                // If both are positive or negative, add to the same list
                if (cold > 0 == hot > 0 || cold == 0 || hot == 0)
                {
                    if (cold > 0 || hot > 0)
                        positiveInsul.add(new Triplet<>(cold, hot, null));
                    else
                        negativeInsul.add(new Triplet<>(-cold, hot, null));
                }
                // If one is positive and one is negative, split them into two lists
                else
                {
                    if (cold > 0)
                        positiveInsul.add(new Triplet<>(cold, 0.0, null));
                    else
                        negativeInsul.add(new Triplet<>(-cold, 0.0, null));

                    if (hot > 0)
                        positiveInsul.add(new Triplet<>(0.0, hot, null));
                    else
                        negativeInsul.add(new Triplet<>(0.0, -hot, null));
                }
            }
            else if (value instanceof ItemInsulationCap.AdaptiveInsulation)
            {
                ItemInsulationCap.AdaptiveInsulation insul = (ItemInsulationCap.AdaptiveInsulation) value;
                double insulation = insul.getInsulation();
                double factor = insul.getFactor();
                double hot = CSMath.blend(0, insulation, factor, -1, 1);
                double cold = CSMath.blend(insulation, 0, factor, -1, 1);
                // If positive, add to positive list, else add to negative list
                if (insulation >= 0)
                {   positiveInsul.add(new Triplet<>(cold, hot, factor));
                }
                else
                {   negativeInsul.add(new Triplet<>(cold, -hot, factor));
                }
            }
        }

        // Render the bars
        matrixStack.pushPose();

        // Positive (default) insulation bar
        int posSlots = positiveInsul.size();
        // Negative insulation bar
        int negSlots = negativeInsul.size();

        if (posSlots > 0)
        {
            drawInsulationBar(matrixStack, x, y, slots, positiveInsul, negativeInsul.size() > 0, false);
            matrixStack.translate(Math.max(slots, posSlots) * 6 + 12, 0, 0);
            width += posSlots * 6 + 12;
        }

        if (negSlots > 0)
        {
            drawInsulationBar(matrixStack, x, y, slots, negativeInsul, true, true);
            width += negSlots * 6 + 12;
        }

        matrixStack.popPose();
    }

    void drawInsulationBar(MatrixStack poseStack, int x, int y, int armorSlots, List<Triplet<Double, Double, Double>> insul, boolean drawSign, boolean isNegative)
    {
        int slots = insul.size();

        if (slots > 0)
        {
            // background
            for (int i = 0; i < Math.max(armorSlots, slots); i++)
            {   AbstractGui.blit(poseStack, x + 7 + i*6, y + 1, 401, 0, 0, 6, 4, 32, 24);
            }

            for (int i = 0; i < slots; i++)
            {
                Triplet<Double, Double, Double> value = insul.get(i);
                double cold = value.getFirst();
                double hot = value.getSecond();
                Double factor = value.getThird();
                boolean isAdaptive = factor != null;
                int cellU;

                if (isAdaptive)
                {
                    int cellV = cold + hot >= 2 ? 16 : 20;
                    float alpha = (float) Math.abs(factor);
                    cellU = factor < 0 ? 6 : 18;

                    // Draw base green underneath
                    AbstractGui.blit(poseStack, x + 7 + i * 6, y + 1, 401, 12, cellV, 6, 4, 24, 32);

                    // Draw either hot/cold texture ontop with alpha
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.color4f(1.0f, 1.0f, 1.0f, alpha);
                    AbstractGui.blit(poseStack, x + 7 + i * 6, y + 1, 401, cellU, cellV, 6, 4, 24, 32);
                    RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                    RenderSystem.disableBlend();
                }
                else
                {
                    int cellV = cold + hot >= 2 ? 8 : 12;
                    cellU = // neutral
                            cold == hot ? 6
                            // cold
                            : Math.abs(cold) > Math.abs(hot) ? 12
                            // hot
                            : 18;
                    AbstractGui.blit(poseStack, x + 7 + i * 6, y + 1, 401, cellU, cellV, 6, 4, 24, 32);
                }
            }

            // border
            for (int i = 0; i < Math.max(armorSlots, slots); i++)
            {
                boolean end = i == Math.max(armorSlots, slots) - 1;
                AbstractGui.blit(poseStack, x + 7 + i*6, y, 401, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 24, 32);
            }
            // icon
            AbstractGui.blit(poseStack, x, y - 1, 401, 24, 8, 8, 8, 24, 32);

            this.width += slots * 6 + 12;

            // sign
            if (drawSign)
            {
                if (isNegative)
                {   AbstractGui.blit(poseStack, x + 3, y + 3, 401, 19, 5, 5, 3, 24, 32);
                }
                else AbstractGui.blit(poseStack, x + 3, y + 2, 401, 19, 0, 5, 5, 24, 32);
            }
        }
    }
}

