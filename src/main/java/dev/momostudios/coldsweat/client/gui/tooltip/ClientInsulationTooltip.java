package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
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
        return ConfigSettings.INSULATION_SLOTS.get()[3 - LivingEntity.getEquipmentSlotForItem(stack).getIndex()] * 6 + 8;
    }

    public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int depth)
    {
        RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png"));

        int slots = ConfigSettings.INSULATION_SLOTS.get()[3 - LivingEntity.getEquipmentSlotForItem(stack).getIndex()];

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
                {   negativeInsul.add(new Triplet<>(cold, -hot, factor));
                }
            }
        }

        // Render the bars
        poseStack.pushPose();

        // Positive (default) insulation bar
        int posSlots = positiveInsul.size();
        // Negative insulation bar
        int negSlots = negativeInsul.size();

        if (posSlots > 0)
        {
            drawInsulationBar(poseStack, x, y, slots, positiveInsul, negativeInsul.size() > 0, false);
            poseStack.translate(Math.max(slots, posSlots) * 6 + 12, 0, 0);
            width += posSlots * 6 + 12;
        }

        if (negSlots > 0)
        {
            drawInsulationBar(poseStack, x, y, slots, negativeInsul, true, true);
            width += negSlots * 6 + 12;
        }

        poseStack.popPose();
    }

    void drawInsulationBar(PoseStack poseStack, int x, int y, int armorSlots, List<Triplet<Double, Double, Double>> insul, boolean drawSign, boolean isNegative)
    {
        int slots = insul.size();

        if (slots > 0)
        {
            for (int i = 0; i < Math.max(armorSlots, slots); i++)
            {
                // background
                GuiComponent.blit(poseStack, x + 7 + i*6, y + 1, 0, 0, 0, 6, 4, 32, 24);
            }

            for (int i = 0; i < slots; i++)
            {
                Triplet<Double, Double, Double> value = insul.get(i);
                double cold = value.getA();
                double hot = value.getB();
                Double factor = value.getC();
                boolean isAdaptive = factor != null;
                int cellU;

                if (isAdaptive)
                {
                    int cellV = cold + hot >= 2 ? 16 : 20;
                    float alpha = (float) Math.abs(factor);
                    cellU = factor < 0 ? 6 : 18;

                    // Draw base green underneath
                    GuiComponent.blit(poseStack, x + 7 + i * 6, y + 1, 0, 12, cellV, 6, 4, 32, 24);

                    // Draw either hot/cold texture ontop with alpha
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
                    GuiComponent.blit(poseStack, x + 7 + i * 6, y + 1, 0, cellU, cellV, 6, 4, 32, 24);
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
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
                    GuiComponent.blit(poseStack, x + 7 + i * 6, y + 1, 0, cellU, cellV, 6, 4, 32, 24);
                }
            }

            // border
            for (int i = 0; i < Math.max(armorSlots, slots); i++)
            {
                boolean end = i == Math.max(armorSlots, slots) - 1;
                GuiComponent.blit(poseStack, x + 7 + i*6, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 32, 24);
            }
            // icon
            GuiComponent.blit(poseStack, x, y - 1, 0, 24, 8, 8, 8, 32, 24);

            this.width += slots * 6 + 12;

            // sign
            if (drawSign)
            {
                if (isNegative)
                    GuiComponent.blit(poseStack, x + 3, y + 3, 0, 19, 5, 5, 3, 32, 24);
                else
                    GuiComponent.blit(poseStack, x + 3, y + 2, 0, 19, 0, 5, 5, 32, 24);
            }
        }
    }
}

