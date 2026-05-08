package com.momosoftworks.coldsweat.client.gui.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class ConditionalTooltip implements TooltipComponent
{
    Component original;
    Font font;
    boolean strikethrough;
    Icon icon;

    public ConditionalTooltip(Component original, Font font, boolean strikethrough, Icon icon)
    {   this.original = original;
        this.font = font;
        this.strikethrough = strikethrough;
        this.icon = icon;
    }

    public ConditionalTooltip(Component original, Font font, boolean strikethrough)
    {   this(original, font, strikethrough, null);
    }

    public Component getOriginal()
    {   return this.original;
    }

    public Font getFont()
    {   return this.font;
    }

    public boolean isStrikethrough()
    {   return this.strikethrough;
    }

    public Icon getIcon()
    {   return this.icon;
    }
}
