package com.momosoftworks.coldsweat.client.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class ImageWidget extends Widget
{
    ResourceLocation texture;
    int u, v;

    public ImageWidget(ResourceLocation texture, int x, int y, int width, int height, int u, int v)
    {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.texture = texture;
        this.u = u;
        this.v = v;
    }

    @Override
    public void render(MatrixStack poseStack, int x, int y, float partialTick)
    {   Minecraft.getInstance().textureManager.bind(this.texture);
        this.blit(poseStack, this.x, this.y, this.u, this.v, this.width, this.height);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {   return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }
}
