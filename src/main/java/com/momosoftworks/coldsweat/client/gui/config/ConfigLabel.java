package com.momosoftworks.coldsweat.client.gui.config;

import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class ConfigLabel extends AbstractWidget implements GuiEventListener, NarratableEntry
{
    public final String id;

    public ConfigLabel(String id, Component text, int x, int y)
    {
        super(x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, text);
        this.id = id;
        this.setX(x);
        this.setY(y);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int pmousex, int mouseY, float partialTick)
    {
        int color = CSMath.getIfNotNull(this.getMessage().getStyle().getColor(), TextColor::getValue, 0xFFFFFF);
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
