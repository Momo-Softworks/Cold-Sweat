package com.momosoftworks.coldsweat.client.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class ConfigLabel extends AbstractWidget implements Widget, GuiEventListener, NarratableEntry
{
    public final String id;

    public ConfigLabel(String id, Component text, int x, int y)
    {
        super(x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, text);
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float depth)
    {
        int color = CSMath.getIfNotNull(this.getMessage().getStyle().getColor(), TextColor::getValue, 0xFFFFFF);
        Minecraft.getInstance().font.drawShadow(poseStack, this.getMessage(), this.x, this.y, color);
    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narration)
    {
        narration.add(NarratedElementType.HINT, this.getMessage());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return mouseX >= this.x - 5 && mouseY >= this.y - 5 && mouseX < this.x + Minecraft.getInstance().font.width(this.getMessage()) + 5 && mouseY < this.y + Minecraft.getInstance().font.lineHeight + 5;
    }
}
