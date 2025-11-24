package com.momosoftworks.coldsweat.client.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.client.gui.util.WidgetSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;

public class ConfigImageButton extends ImageButton
{
    WidgetSprites sprites;
    protected int textureWidth;
    protected int textureHeight;

    public ConfigImageButton(int x, int y, int width, int height, WidgetSprites sprites, int imageWidth, int imageHeight, Button.IPressable onPress)
    {   super(x, y, width, height, 0, 0, 0, null, imageWidth, imageHeight, onPress);
        this.textureWidth = imageWidth;
        this.textureHeight = imageHeight;
        this.sprites = sprites;
    }

    protected boolean isEnabled()
    {   return true;
    }

    @Override
    public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        ResourceLocation resourcelocation = this.sprites.get(this.isEnabled(), this.isFocused() || this.isHovered());
        Minecraft.getInstance().textureManager.bind(resourcelocation);
        RenderSystem.enableDepthTest();
        AbstractGui.blit(poseStack, this.x, this.y, 0, 0, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered)
        {   this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}
