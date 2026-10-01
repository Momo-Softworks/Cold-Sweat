package com.momosoftworks.coldsweat.client.gui.config;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ConfigImageWidget extends AbstractWidget implements GuiEventListener, NarratableEntry
{
    Identifier texture;
    int x, y, width, height, u, v;

    public ConfigImageWidget(Identifier texture, int x, int y, int width, int height, int u, int v)
    {
        super(x, y, width, height, Component.empty());
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {   graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.x, this.y, u, v, width, height, width, height);
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
