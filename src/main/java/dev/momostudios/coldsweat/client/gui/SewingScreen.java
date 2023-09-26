package dev.momostudios.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.container.SewingContainer;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class SewingScreen extends AbstractContainerScreen<SewingContainer>
{
    private static final ResourceLocation SEWING_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/sewing_gui.png");

    public SewingScreen(SewingContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 175;
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
        this.titleLabelX = this.getXSize() / 2 - this.font.width(this.title) / 2;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, SEWING_GUI);
        int x = (this.width - this.getXSize()) / 2;
        int y = (this.height - this.getYSize()) / 2;
        this.blit(poseStack, x, y, 0, 0, this.getXSize(), this.getYSize());

        if (!menu.getSlot(0).hasItem())
        {   this.blit(poseStack, this.getGuiLeft() + 43, this.getGuiTop() + 26, 176, 0, 16, 16);
        }
        if (!menu.getSlot(1).hasItem())
        {   this.blit(poseStack, this.getGuiLeft() + 43, this.getGuiTop() + 53, 192, 0, 16, 16);
        }
    }
}
