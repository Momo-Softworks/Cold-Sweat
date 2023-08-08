package dev.momostudios.coldsweat.client.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public class ConfigLabel extends AbstractWidget implements Widget, GuiEventListener, NarratableEntry
{
    public final String id;

    private int color;

    public ConfigLabel(String id, String text, int x, int y)
    {
        this(id, text, x, y, 16777215);
    }

    public ConfigLabel(String id, String text, int x, int y, int color)
    {
        super(x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, new TextComponent(text));
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void setTextColor(int color)
    {
        this.color = color;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float depth)
    {
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
