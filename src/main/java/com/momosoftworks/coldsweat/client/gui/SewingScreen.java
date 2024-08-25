package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.SewingContainer;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class SewingScreen extends AbstractContainerScreen<SewingContainer>
{
    private static final ResourceLocation SEWING_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/sewing_gui.png");
    TranslatableComponent name = new TranslatableComponent("container." + ColdSweat.MOD_ID + ".sewing_table");

    public SewingScreen(SewingContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, new TranslatableComponent("container." + ColdSweat.MOD_ID + ".sewing_table"));
        this.imageWidth = 176;
        this.imageHeight = 201;
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void init()
    {
        super.init();
        this.titleLabelX = this.getXSize() / 2 - this.font.width(name) / 2;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, SEWING_GUI);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.getXSize(), this.getYSize());

        if (!menu.getSlot(0).hasItem())
        {
            this.blit(poseStack, this.getGuiLeft() + 43, this.getGuiTop() + 26, 176, 0, 16, 16);
        }
        if (!menu.getSlot(1).hasItem())
        {
            this.blit(poseStack, this.getGuiLeft() + 43, this.getGuiTop() + 53, 192, 0, 16, 16);
        }
    }
}
