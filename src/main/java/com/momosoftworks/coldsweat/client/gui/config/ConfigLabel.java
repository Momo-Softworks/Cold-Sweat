package com.momosoftworks.coldsweat.client.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

public class ConfigLabel extends Widget
{
    public final String id;

    private int color;

    public ConfigLabel(String id, String text, int x, int y)
    {
        this(id, text, x, y, 16777215);
    }

    public ConfigLabel(String id, String text, int x, int y, int color)
    {
        super(x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, new StringTextComponent(text));
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void setTextColor(int color)
    {
        this.color = color;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float depth)
    {   Minecraft.getInstance().font.drawShadow(matrixStack, this.getMessage(), this.x, this.y, color);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {   return mouseX >= this.x - 5 && mouseY >= this.y - 5 && mouseX < this.x + Minecraft.getInstance().font.width(this.getMessage()) + 5 && mouseY < this.y + Minecraft.getInstance().font.lineHeight + 5;
    }
}
