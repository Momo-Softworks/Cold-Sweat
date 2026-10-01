package com.momosoftworks.coldsweat.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.IceboxContainer;
import net.minecraft.world.entity.player.Inventory;

public class IceboxScreen extends AbstractHearthScreen<IceboxContainer>
{
    private static final Identifier ICEBOX_GUI = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/screen/icebox_gui.png");

    @Override
    HearthBlockEntity getBlockEntity()
    {   return this.menu.te;
    }

    public IceboxScreen(IceboxContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, titleIn, 176, 172);
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = leftPos + this.imageWidth / 2 - Minecraft.getInstance().font.width(this.getTitle()) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICEBOX_GUI, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        // Draw fuel gauge
        this.renderFuelGauge(HearthBlockEntity.FuelType.COLD, graphics, leftPos + 100,  topPos + 63, this.menu.getFuel(), this.menu.te.getMaxFuel());

        if (!ConfigSettings.SMART_HEARTH.get() && this.menu.te.hasSmokestack())
        {
            boolean powered = this.menu.te.isCoolingOn();

            this.renderPowerIndicator(graphics, leftPos + 101, topPos + 78, powered);

            if (CSMath.betweenInclusive(mouseX, leftPos + 98, leftPos + 117) && CSMath.betweenInclusive(mouseY, topPos + 75, topPos + 82))
            {   graphics.setTooltipForNextFrame(Component.translatable(powered ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"), mouseX, mouseY);
            }
        }
    }
}
