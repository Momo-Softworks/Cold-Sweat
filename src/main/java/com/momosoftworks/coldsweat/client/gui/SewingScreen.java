package com.momosoftworks.coldsweat.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.gui.util.CyclingSlotBackground;
import com.momosoftworks.coldsweat.common.container.SewingContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class SewingScreen extends AbstractContainerScreen<SewingContainer>
{
    private static final Identifier SEWING_GUI = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/screen/sewing_gui.png");
    private static final Identifier ARMOR_ICON = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/sewing/sewing_armor_slot.png");
    private static final Identifier LEATHER_ICON = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/sewing/sewing_insulator_slot.png");
    private static final Identifier SHEARS_ICON = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/sewing/sewing_shears_slot.png");

    CyclingSlotBackground insulatorBackground;
    CyclingSlotBackground armorBackground;

    public SewingScreen(SewingContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, titleIn, 176, 201);
        this.insulatorBackground = new CyclingSlotBackground(1, List.of(LEATHER_ICON, SHEARS_ICON));
        this.armorBackground = new CyclingSlotBackground(0, List.of(ARMOR_ICON));
    }

    @Override
    protected void init()
    {
        super.init();
        this.titleLabelX = this.getXSize() / 2 - this.font.width(this.title) / 2;
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        this.insulatorBackground.tick();
        this.armorBackground.tick();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SEWING_GUI, this.leftPos, this.topPos, 0, 0, this.getXSize(), this.getYSize(), 256, 256);

        this.armorBackground.render(this.getMenu(), graphics, partialTicks, this.getGuiLeft(), this.getGuiTop());
        this.insulatorBackground.render(this.getMenu(), graphics, partialTicks, this.getGuiLeft(), this.getGuiTop());
    }
}
