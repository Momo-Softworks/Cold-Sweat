package dev.momostudios.coldsweat.client.gui.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.resources.ResourceLocation;

public class ConfigImage extends GuiComponent implements Widget
{
    ResourceLocation texture;
    int x, y, width, height, u, v;

    public ConfigImage(ResourceLocation texture, int x, int y, int width, int height, int u, int v)
    {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, float partialTick)
    {
        RenderSystem.setShaderTexture(0, texture);
        this.blit(poseStack, this.x, this.y, u, v, width, height);
    }
}
