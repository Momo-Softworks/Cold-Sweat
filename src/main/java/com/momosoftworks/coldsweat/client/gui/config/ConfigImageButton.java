package com.momosoftworks.coldsweat.client.gui.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.client.gui.util.WidgetSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ConfigImageButton extends ImageButton
{
    WidgetSprites sprites;
    protected int textureWidth;
    protected int textureHeight;

    public ConfigImageButton(int x, int y, int width, int height, WidgetSprites sprites, int imageWidth, int imageHeight, OnPress onPress)
    {   super(x, y, width, height, 0, 0, 0, null, imageWidth, imageHeight, onPress);
        this.textureWidth = imageWidth;
        this.textureHeight = imageHeight;
        this.sprites = sprites;
    }

    protected boolean isEnabled()
    {   return true;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        ResourceLocation resourcelocation = this.sprites.get(this.isEnabled(), this.isHoveredOrFocused());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, resourcelocation);
        RenderSystem.enableDepthTest();
        Screen.blit(poseStack, this.x, this.y, 0, 0, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered)
        {   this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}
