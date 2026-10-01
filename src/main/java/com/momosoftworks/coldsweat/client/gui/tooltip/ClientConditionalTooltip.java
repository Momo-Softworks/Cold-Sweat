package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.util.Optional;

public class ClientConditionalTooltip implements ClientTooltipComponent
{
    Component original;
    Font font;
    boolean strikethrough;
    Icon icon;

    public ClientConditionalTooltip(Component original, Font font, boolean strikethrough, Icon icon)
    {   this.original = original;
        this.font = font;
        this.strikethrough = strikethrough;
        this.icon = icon;
    }

    @Override
    public int getHeight(Font font)
    {   return this.font.lineHeight + 2;
    }

    @Override
    public int getWidth(Font font)
    {   return this.font.width(this.original) + 10;
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics)
    {
        // Icon
        if (icon != null)
        {   icon.render(graphics, x, y, 0);
        }
        // Text
        int color = strikethrough ? 7561572 : Optional.ofNullable(this.original.getStyle().getColor()).map(TextColor::getValue).orElse(0xFFFFFF);
        if (icon != null)
        {
            if (this.strikethrough)
            {   graphics.fill(x - 2, y + 3, x + 9, y + 4, 0xFFF63232);
                graphics.fill(x - 1, y + 4, x + 10, y + 5, 0xFFF63232);
                x += 2;
            }
            graphics.text(font, this.original.getString(), x + 10, y + 1, ClientOnlyHelper.legacyTextColor(color));
        }
        else
        {
            int strikeLength = font.width(this.original) + 2;
            graphics.text(font, this.original.getString(), x, y + 1, ClientOnlyHelper.legacyTextColor(color));
            if (strikethrough)
            {   graphics.fill(x - 1, y + 5, x - 1 + strikeLength, y + 6, 0xFFF63232);
            }
        }
    }
}
