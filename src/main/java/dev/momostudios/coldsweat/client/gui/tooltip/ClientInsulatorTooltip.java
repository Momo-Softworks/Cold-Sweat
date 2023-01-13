package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.TextComponent;
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

    public ClientInsulatorTooltip(Pair<Double, Double> insulationValues)
    {
        this.insulationValues = insulationValues;
    }

    @Override
    public int getHeight()
    {
        return 10;
    }

    @Override
    public int getWidth(Font font)
    {
        return 0;
    }

    public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int depth)
    {
        cold = insulationValues.getFirst();
        hot = insulationValues.getSecond();
        neutral = (cold > 0 == hot > 0 ? CSMath.smallest(cold, hot) : 0) * 2;
        hot -= neutral/2;
        cold -= neutral/2;
        double coldSlots = Math.abs(cold/2);
        double hotSlots = Math.abs(hot/2);
        double neutralSlots = Math.abs(neutral/2);
       RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png"));

        int slots = CSMath.ceil(CSMath.max(coldSlots, hotSlots, neutralSlots));

        for (int i = 0; i < slots; i++)
        {
            // background
            GuiComponent.blit(poseStack, x + 7 + i*6, y + 1, 0, 0, 1, 6, 4, 32, 16);
        }

        // cold
        for (int i = 0; i < CSMath.ceil(cold/2); i++)
        {
            int uvX = 6;
            int uvY = cold - i * CSMath.getSign(cold) * 2 > 1 ? 7 : 11;

            GuiComponent.blit(poseStack, x + 7 + i*6, y + 1, 0, uvX, uvY, 6, 4, 32, 16);
        }

        // neutral
        for (int i = 0; i < CSMath.ceil(neutral/2); i++)
        {
            int uvX = 0;
            int uvY = neutral - i * CSMath.getSign(neutral) * 2 > 1 ? 7 : 11;

            GuiComponent.blit(poseStack, (int) (x + 7 + i*6 + coldSlots * 6), y + 1, 0, uvX, uvY, 6, 4, 32, 16);
        }

        // hot
        for (int i = 0; i < CSMath.ceil(hot/2); i++)
        {
            int uvX = 12;
            int uvY = hot - i * CSMath.getSign(hot) * 2 > 1 ? 7 : 11;

            GuiComponent.blit(poseStack, (int) (x + 7 + i*6 + coldSlots * 6 + neutralSlots * 6), y + 1, 0, uvX, uvY, 6, 4, 32, 16);
        }

        for (int i = 0; i < slots; i++)
        {
            boolean end = i == slots - 1;
            // border
            GuiComponent.blit(poseStack, x + 7 + i*6, y, 0, (end ? 12 : 6), 0, (end ? 7 : 6), 6, 32, 16);
        }
        // icon
        GuiComponent.blit(poseStack, x, y - 1, 0, 24, 0, 8, 8, 32, 16);
    }
}
