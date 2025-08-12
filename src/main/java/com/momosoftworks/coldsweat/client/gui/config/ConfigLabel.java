package com.momosoftworks.coldsweat.client.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;

public class ConfigLabel extends Widget
{
    public final String id;

    public ConfigLabel(String id, IFormattableTextComponent text, int x, int y)
    {
        super(x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, text);
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float depth)
    {
        int color = CSMath.getIfNotNull(this.getMessage().getStyle().getColor(), Color::getValue, 0xFFFFFF);
        Minecraft.getInstance().font.drawShadow(poseStack, this.getMessage(), this.x, this.y, color);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {   return mouseX >= this.x - 5 && mouseY >= this.y - 5 && mouseX < this.x + Minecraft.getInstance().font.width(this.getMessage()) + 5 && mouseY < this.y + Minecraft.getInstance().font.lineHeight + 5;
    }
}
