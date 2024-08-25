package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.SewingContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

public class SewingScreen extends ContainerScreen<SewingContainer>
{
    private static final ResourceLocation SEWING_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/sewing_gui.png");
    static final TranslationTextComponent NAME = new TranslationTextComponent("container." + ColdSweat.MOD_ID + ".sewing_table");

    public SewingScreen(SewingContainer screenContainer, PlayerInventory inv, ITextComponent title)
    {
        super(screenContainer, inv, NAME);
        this.imageWidth = 176;
        this.imageHeight = 201;
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void init()
    {
        super.init();
        this.titleLabelX = this.getXSize() / 2 - this.font.width(NAME) / 2;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        this.minecraft.textureManager.bind(SEWING_GUI);
        this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.getXSize(), this.getYSize());

        if (!menu.getSlot(0).hasItem())
        {   this.blit(matrixStack, this.getGuiLeft() + 43, this.getGuiTop() + 26, 176, 0, 16, 16);
        }
        if (!menu.getSlot(1).hasItem())
        {   this.blit(matrixStack, this.getGuiLeft() + 43, this.getGuiTop() + 53, 192, 0, 16, 16);
        }
    }
}
