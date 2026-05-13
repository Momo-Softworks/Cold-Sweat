package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ClientConditionalTooltip extends Tooltip
{
    IFormattableTextComponent original;
    FontRenderer font;
    boolean strikethrough;
    Icon icon;

    public ClientConditionalTooltip(IFormattableTextComponent original, FontRenderer font, boolean strikethrough, Icon icon)
    {   this.original = original;
        this.font = font;
        this.strikethrough = strikethrough;
        this.icon = icon;
    }

    @Override
    public int getHeight()
    {   return this.font.lineHeight + 2;
    }

    @Override
    public int getWidth(FontRenderer font)
    {   return this.font.width(this.original) + 10;
    }

    @Override
    public void renderImage(FontRenderer font, int x, int y, MatrixStack ps, ItemRenderer itemRenderer, int z)
    {
        // Icon
        ps.translate(0, 0, 401);
        if (icon != null)
        {   icon.render(ps, x, y, z);
        }
        // Text
        int color = strikethrough ? 7561572 : Optional.ofNullable(this.original.getStyle().getColor()).map(Color::getValue).orElse(0xFFFFFF);
        if (icon != null)
        {
            if (this.strikethrough)
            {   Screen.fill(ps, x - 2, y + 3, x + 9, y + 4, 0xFFF63232);
                Screen.fill(ps, x - 1, y + 4, x + 10, y + 5, 0xFFF63232);
                x += 2;
            }
            font.drawShadow(ps, this.original.getString(), x + 10, y + 1, color);
        }
        else
        {
            int strikeLength = font.width(this.original) + 2;
            font.drawShadow(ps, this.original.getString(), x, y + 1, color);
            if (strikethrough)
            {   Screen.fill(ps, x - 1, y + 5, x - 1 + strikeLength, y + 6, 0xFFF63232);
            }
        }
        ps.translate(0, 0, -401);
    }
}
