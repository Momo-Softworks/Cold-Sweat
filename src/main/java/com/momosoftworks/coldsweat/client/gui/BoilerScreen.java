package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class BoilerScreen extends ContainerScreen<BoilerContainer>
{
    private static final ResourceLocation BOILER_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/boiler_gui.png");

    public BoilerScreen(BoilerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 176;
        this.imageHeight = 172;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = leftPos + this.imageWidth / 2 - Minecraft.getInstance().font.width(this.getTitle()) / 2;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        Minecraft.getInstance().textureManager.bind(BOILER_GUI);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Draw fuel gauge
        blit(matrixStack, leftPos + 109, topPos + 63, 176, 0, (int) (this.menu.getFuel() / 31.25), 16, 256, 256);
    }
}
