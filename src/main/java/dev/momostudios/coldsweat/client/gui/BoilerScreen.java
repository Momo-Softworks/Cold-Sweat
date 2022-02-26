package dev.momostudios.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.container.BoilerContainer;
import net.minecraft.world.entity.player.Inventory;

public class BoilerScreen extends AbstractContainerScreen<BoilerContainer>
{
    private static final ResourceLocation BOILER_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/boiler_gui.png");
    private static final ResourceLocation FUEL_GAUGE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/lava_gauge.png");
    Component name = new TranslatableComponent("block." + ColdSweat.MOD_ID + ".boiler");
    int fuelLevel;

    public BoilerScreen(BoilerContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 175;
        this.imageHeight = 201;
        this.fuelLevel = screenContainer.te.getFuel();
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);

        this.font.draw(matrixStack, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY + 6, 4210752);
        this.font.draw(matrixStack, name, 88 - font.width(name.getString()) / 2f, 9f, 4210752);

        this.minecraft.textureManager.bindForSetup(FUEL_GAUGE);
        this.blit(matrixStack, 108, 62, 0, 0, (int) (this.menu.getFuel() / 31.25), 16, 32, 16);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        this.minecraft.textureManager.bindForSetup(BOILER_GUI);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, x ,y, 0, 0, this.imageWidth, this.imageHeight);
    }
}
