package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.gui.util.CyclingSlotBackground;
import com.momosoftworks.coldsweat.common.container.SewingContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class SewingScreen extends ContainerScreen<SewingContainer>
{
    private static final ResourceLocation SEWING_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/sewing_gui.png");
    private static ResourceLocation ARMOR_ICON = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/sprites/sewing/sewing_armor_slot.png");
    private static ResourceLocation LEATHER_ICON = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/sprites/sewing/sewing_insulator_slot.png");
    private static ResourceLocation SHEARS_ICON = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/sprites/sewing/sewing_shears_slot.png");

    CyclingSlotBackground insulatorBackground;
    CyclingSlotBackground armorBackground;

    public SewingScreen(SewingContainer screenContainer, PlayerInventory inv, ITextComponent title)
    {
        super(screenContainer, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 201;
        this.insulatorBackground = new CyclingSlotBackground(1, Arrays.asList(LEATHER_ICON, SHEARS_ICON));
        this.armorBackground = new CyclingSlotBackground(0, Arrays.asList(ARMOR_ICON));
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
        this.titleLabelX = this.getXSize() / 2 - this.font.width(this.title) / 2;
    }

    @Override
    public void tick()
    {
        super.tick();
        this.insulatorBackground.tick();
        this.armorBackground.tick();
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        this.minecraft.textureManager.bind(SEWING_GUI);
        this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.getXSize(), this.getYSize());

        this.armorBackground.render(this.getMenu(), matrixStack, partialTicks, this.getGuiLeft(), this.getGuiTop());
        this.insulatorBackground.render(this.getMenu(), matrixStack, partialTicks, this.getGuiLeft(), this.getGuiTop());
    }
}
