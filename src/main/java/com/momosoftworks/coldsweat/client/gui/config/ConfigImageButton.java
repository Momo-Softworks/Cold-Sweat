package com.momosoftworks.coldsweat.client.gui.config;

import com.momosoftworks.coldsweat.client.gui.util.WidgetSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

public class ConfigImageButton extends ImageButton
{
    WidgetSprites sprites;

    public ConfigImageButton(int x, int y, int width, int height, WidgetSprites sprites, int imageWidth, int imageHeight, OnPress onPress)
    {   super(x, y, width, height, 0, 0, 0, null, imageWidth, imageHeight, onPress);
        this.sprites = sprites;
    }

    protected boolean isEnabled()
    {   return true;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        ResourceLocation resourcelocation = this.sprites.get(this.isEnabled(), this.isHoveredOrFocused());
        guiGraphics.blit(resourcelocation, this.getX(), this.getY(), 0, 0, this.width, this.height, this.textureWidth, this.textureHeight);
    }
}
