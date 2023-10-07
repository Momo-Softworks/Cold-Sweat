package com.momosoftworks.coldsweat.client.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ConfigLabel extends AbstractWidget implements GuiEventListener, NarratableEntry
{
    public final String id;

    private int color;

    public ConfigLabel(String id, String text, int x, int y)
    {
        this(id, text, x, y, 16777215);
    }

    public ConfigLabel(String id, String text, int x, int y, int color)
    {
        super(x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, Component.translatable(text));
        this.id = id;
        this.setX(x);
        this.setY(y);
        this.color = color;
    }

    public void setTextColor(int color)
    {
        this.color = color;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int pmousex, int mouseY, float partialTick)
    {
        graphics.drawString(Minecraft.getInstance().font, this.getMessage(), this.getX(), this.getY(), color, true);
    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narration)
    {
        narration.add(NarratedElementType.HINT, this.getMessage());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return mouseX >= this.getX() - 5 && mouseY >= this.getY() - 5 && mouseX < this.getX() + Minecraft.getInstance().font.width(this.getMessage()) + 5 && mouseY < this.getY() + Minecraft.getInstance().font.lineHeight + 5;
    }
}
