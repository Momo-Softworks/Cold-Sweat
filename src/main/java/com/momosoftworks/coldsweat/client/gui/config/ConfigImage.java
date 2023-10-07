package com.momosoftworks.coldsweat.client.gui.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;

public class ConfigImage extends ImageWidget implements GuiEventListener, NarratableEntry
{
    ResourceLocation texture;
    int x, y, width, height, u, v;

    public ConfigImage(ResourceLocation texture, int x, int y, int width, int height, int u, int v)
    {   super(x, y, width, height, texture);
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {   graphics.blit(texture, this.x, this.y, u, v, width, height);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {   return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    @Override
    public NarrationPriority narrationPriority()
    {   return NarrationPriority.HOVERED;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narration)
    {   narration.add(NarratedElementType.HINT, "image");
    }
}
