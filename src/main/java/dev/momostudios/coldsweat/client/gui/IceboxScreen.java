package dev.momostudios.coldsweat.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.container.IceboxContainer;

public class IceboxScreen extends ContainerScreen<IceboxContainer>
{
    private static final ResourceLocation ICEBOX_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/icebox_gui.png");
    private static final ResourceLocation FUEL_GAUGE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/water_gauge.png");
    ITextComponent name = new TranslationTextComponent("block." + ColdSweat.MOD_ID + ".icebox");
    int titleX = this.xSize / 2 - name.toString().length() / 18;
    int fuelLevel;
    public IceboxScreen(IceboxContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.guiLeft = 0;
        this.guiTop = 0;
        this.xSize = 175;
        this.ySize = 201;
        this.fuelLevel = screenContainer.te.getTileData().getInt("fuel");
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y)
    {
        this.font.drawText(matrixStack, this.playerInventory.getDisplayName(), (float) this.playerInventoryTitleX, (float) this.playerInventoryTitleY + 6, 4210752);
        this.font.drawText(matrixStack, name, 88 - font.getStringWidth(name.getString()) / 2f, 9f, 4210752);

        this.minecraft.textureManager.bindTexture(FUEL_GAUGE);
        blit(matrixStack, 108, 62, 0, 0, (int) (this.container.getFuel() / 31.25), 16, 32, 16);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        this.minecraft.textureManager.bindTexture(ICEBOX_GUI);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(matrixStack, x ,y, 0, 0, this.xSize, this.ySize);
    }
}
