package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;

public abstract class Tooltip
{
    public abstract int getHeight();

    public abstract int getWidth(FontRenderer font);

    public void renderImage(FontRenderer font, int x, int y, MatrixStack matrixStack, ItemRenderer itemRenderer, int depth) {}
    public void renderText(FontRenderer font, int x, int y, MatrixStack matrixStack, ItemRenderer itemRenderer, int depth) {}
}
