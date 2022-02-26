package dev.momostudios.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.container.SewingContainer;
import net.minecraft.world.entity.player.Inventory;

public class SewingScreen extends AbstractContainerScreen<SewingContainer>
{
    private static final ResourceLocation SEWING_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/sewing_gui.png");
    TranslatableComponent name = new TranslatableComponent("block." + ColdSweat.MOD_ID + ".sewing_table");

    ImageButton sewButton;

    public SewingScreen(SewingContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 175;
        this.imageHeight = 201;

        /*sewButton = new ImageButton(this.getGuiLeft() + 145, this.getGuiTop() + 39, 16, 16, 0, 0, 16,
            new ResourceLocation("cold_sweat:textures/gui/screen/sew_button.png"), button ->
            screenContainer.sewItems());
        this.addButton(sewButton);*/
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);

        this.font.draw(poseStack, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
        this.font.draw(poseStack, name, titleLabelX, 8f, 4210752);
    }

    @Override
    protected void init()
    {
        super.init();
        this.titleLabelX = this.getXSize() / 2 - this.font.width(name) / 2;
        //this.children.add(sewButton);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        this.minecraft.textureManager.bindForSetup(SEWING_GUI);
        int x = (this.width - this.getXSize()) / 2;
        int y = (this.height - this.getYSize()) / 2;
        this.blit(poseStack, x ,y, 0, 0, this.getXSize(), this.getYSize());

        if (!menu.getSlot(0).hasItem())
        {
            this.minecraft.textureManager.bindForSetup(new ResourceLocation("cold_sweat:textures/gui/screen/sewing_elements.png"));
            this.blit(poseStack, this.getGuiLeft() + 43, this.getGuiTop() + 26, 0, 0, 16, 16);
        }
        if (!menu.getSlot(1).hasItem())
        {
            this.minecraft.textureManager.bindForSetup(new ResourceLocation("cold_sweat:textures/gui/screen/sewing_elements.png"));
            this.blit(poseStack, this.getGuiLeft() + 43, this.getGuiTop() + 53, 16, 0, 16, 16);
        }
    }
}
