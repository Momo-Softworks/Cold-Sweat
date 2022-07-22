package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientHellspringTooltip implements ClientTooltipComponent
{
    double fuel;

    public ClientHellspringTooltip(double fuel)
    {
        this.fuel = fuel;
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
        RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/tooltip/soulspring_lamp_fuel.png"));
        GuiComponent.blit(poseStack, x, y, 0, 0, 0, 30, 8, 30, 24);
        GuiComponent.blit(poseStack, x, y, 0, 0, 16, (int) (fuel / 2.1333), 8, 30, 24);
    }
}
