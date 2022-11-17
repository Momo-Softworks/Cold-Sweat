package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.common.event.ArmorInsulation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientInsulationTooltip implements ClientTooltipComponent
{
    List<Pair<Double, Double>> insulationValues;
    ItemStack stack;

    public ClientInsulationTooltip(List<Pair<Double, Double>> insulationValues, ItemStack stack)
    {
        this.insulationValues = insulationValues;
        this.stack = stack;
    }

    @Override
    public int getHeight()
    {
        return 0;
    }

    @Override
    public int getWidth(Font font)
    {
        return 0;
    }

    public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int depth)
    {
        RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png"));

        int slots = ArmorInsulation.getInsulationSlots(stack);

        for (int i = 0; i < slots; i++)
        {
            boolean end = i == slots - 1;

            // background
            GuiComponent.blit(poseStack, x + 7 + i*6, y + 1, 0, (end ? 6 : 0), 5, 6, 4, 32, 16);
        }

        for (int i = 0; i < insulationValues.size(); i++)
        {
            Pair<Double, Double> value = insulationValues.get(i);
            double cold = value.getFirst();
            double hot = value.getSecond();
            int uvX = cold == hot ? 0
                    : cold > hot ? 6
                    : 12;

            // cells
            GuiComponent.blit(poseStack, x + 7 + i*6, y + 1, 0, uvX, 0, 6, 4, 32, 16);
        }

        for (int i = 0; i < slots; i++)
        {
            boolean end = i == slots - 1;
            // border
            GuiComponent.blit(poseStack, x + 7 + i*6, y, 0, (end ? 6 : 0), 10, (end ? 7 : 6), 6, 32, 16);
        }
        // icon
        GuiComponent.blit(poseStack, x, y - 1, 0, 24, 0, 8, 8, 32, 16);
    }
}
