package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
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
    public int getHeight()
    {   return this.font.lineHeight + 2;
    }

    @Override
    public int getWidth(Font font)
    {   return this.font.width(this.original) + 10;
    }

    @Override
    public void renderImage(Font font, int x, int y, PoseStack ps, ItemRenderer itemRenderer, int z)
    {
        // Icon
        ps.translate(0, 0, 401);
        if (icon != null)
        {
            RenderSystem.setShaderTexture(0, icon.location());
            Screen.blit(ps, x, y, 0, 28, 8, 8, 8, 36, 28);
        }
        // Text
        int color = strikethrough ? 7561572 : Optional.ofNullable(this.original.getStyle().getColor()).map(TextColor::getValue).orElse(0xFFFFFF);
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
