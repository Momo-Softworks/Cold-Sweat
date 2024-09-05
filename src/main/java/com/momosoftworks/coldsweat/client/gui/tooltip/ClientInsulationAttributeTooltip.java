package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientInsulationAttributeTooltip implements ClientTooltipComponent
{
    public static final ResourceLocation TOOLTIP = ResourceLocation.parse("cold_sweat:textures/gui/tooltip/insulation_bar.png");
    public static final ResourceLocation TOOLTIP_HC = ResourceLocation.parse("cold_sweat:textures/gui/tooltip/insulation_bar_hc.png");
    public static final Supplier<ResourceLocation> TOOLTIP_LOCATION = () ->
            ConfigSettings.HIGH_CONTRAST.get() ? TOOLTIP_HC
                                               : TOOLTIP;

    Component original;
    Font font;

    public ClientInsulationAttributeTooltip(Component original, Font font)
    {   this.original = original;
        this.font = font;
    }

    @Override
    public int getHeight()
    {   return this.font.lineHeight + 2;
    }

    @Override
    public int getWidth(Font font)
    {   return this.font.width(this.original) + 10;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics)
    {
        // Icon
        graphics.blit(TOOLTIP_LOCATION.get(), x, y + 1, 0, 24, 8, 8, 8, 32, 24);
        // Text
        int color = Optional.ofNullable(this.original.getStyle().getColor()).map(TextColor::getValue).orElse(16777215);
        graphics.drawString(font, this.original, x + 10, y + 1, color);
    }
}
