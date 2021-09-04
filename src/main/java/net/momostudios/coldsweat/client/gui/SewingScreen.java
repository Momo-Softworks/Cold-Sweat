package net.momostudios.coldsweat.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.container.SewingContainer;

public class SewingScreen extends ContainerScreen<SewingContainer>
{
    private static final ResourceLocation SEWING_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/sewing_gui.png");
    TranslationTextComponent name = new TranslationTextComponent("block." + ColdSweat.MOD_ID + ".sewing_table");

    ImageButton sewButton;

    public SewingScreen(SewingContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.guiLeft = 0;
        this.guiTop = 0;
        this.xSize = 175;
        this.ySize = 201;

        /*sewButton = new ImageButton(this.getGuiLeft() + 145, this.getGuiTop() + 39, 16, 16, 0, 0, 16,
            new ResourceLocation("cold_sweat:textures/gui/screen/sew_button.png"), button ->
            screenContainer.sewItems());
        this.addButton(sewButton);*/
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        //sewButton.setPosition(this.getGuiLeft() + 145, this.getGuiTop() + 39);
        //sewButton.render(matrixStack, mouseX, mouseY, partialTicks);

        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void init()
    {
        super.init();
        this.titleX = this.xSize / 2 - this.font.getStringPropertyWidth(name) / 2;
        //this.children.add(sewButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y)
    {
        this.font.drawText(matrixStack, this.playerInventory.getDisplayName(), (float) this.playerInventoryTitleX, (float) this.playerInventoryTitleY, 4210752);
        this.font.drawText(matrixStack, name, titleX, 8f, 4210752);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        this.minecraft.textureManager.bindTexture(SEWING_GUI);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(matrixStack, x ,y, 0, 0, this.xSize, this.ySize);

        if (!container.getSlot(0).getHasStack())
        {
            this.minecraft.textureManager.bindTexture(new ResourceLocation("cold_sweat:textures/gui/screen/sewing_elements.png"));
            this.blit(matrixStack, this.getGuiLeft() + 43, this.getGuiTop() + 26, 0, 0, 16, 16);
        }
        if (!container.getSlot(1).getHasStack())
        {
            this.minecraft.textureManager.bindTexture(new ResourceLocation("cold_sweat:textures/gui/screen/sewing_elements.png"));
            this.blit(matrixStack, this.getGuiLeft() + 43, this.getGuiTop() + 53, 16, 0, 16, 16);
        }
    }
}
