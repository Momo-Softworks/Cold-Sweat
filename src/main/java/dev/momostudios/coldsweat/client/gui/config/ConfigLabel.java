package dev.momostudios.coldsweat.client.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class ConfigLabel implements Widget, GuiEventListener, NarratableEntry
{
    public final String id;

    public String text;
    public int color;
    public int x;
    public int y;

    public ConfigLabel(String id, String text, int x, int y)
    {
        this(id, text, x, y, 16777215);
    }

    public ConfigLabel(String id, String text, int x, int y, int color)
    {
        this.id = id;
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float depth)
    {
        Minecraft.getInstance().font.drawShadow(poseStack, this.text, this.x, this.y, color);
    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narration)
    {
        narration.add(NarratedElementType.HINT, this.text);
    }
}
