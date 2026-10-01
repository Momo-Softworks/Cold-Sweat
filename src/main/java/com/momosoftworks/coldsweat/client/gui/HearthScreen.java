package com.momosoftworks.coldsweat.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.container.HearthContainer;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class HearthScreen extends AbstractHearthScreen<HearthContainer>
{
    private static final Identifier HEARTH_GUI = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/screen/hearth_gui.png");

    @Override
    HearthBlockEntity getBlockEntity()
    {   return this.menu.te;
    }

    public HearthScreen(HearthContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, Component.translatable("container." + ColdSweat.MOD_ID + ".hearth"), 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {   super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HEARTH_GUI, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        // Render hot/cold fuel gauges
        this.renderFuelGauge(HearthBlockEntity.FuelType.HOT, graphics, leftPos + 62,  topPos + 49, this.menu.getHotFuel(), this.menu.te.getMaxFuel());
        this.renderFuelGauge(HearthBlockEntity.FuelType.COLD, graphics, leftPos + 100, topPos + 49, this.menu.getColdFuel(), this.menu.te.getMaxFuel());

        // Render redstone indicators
        if (!ConfigSettings.SMART_HEARTH.get())
        {
            boolean coolingOn = this.menu.te.isCoolingOn();
            boolean heatingOn = this.menu.te.isHeatingOn();

            this.renderPowerIndicator(graphics, leftPos + 63, topPos + 64, heatingOn);
            this.renderPowerIndicator(graphics, leftPos + 101, topPos + 64, coolingOn);

            if (CSMath.betweenInclusive(mouseX, leftPos + 60, leftPos + 76) && CSMath.betweenInclusive(mouseY, topPos + 61, topPos + 67))
            {   graphics.setTooltipForNextFrame(Component.translatable(heatingOn ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"), mouseX, mouseY);
            }
            if (CSMath.betweenInclusive(mouseX, leftPos + 98, leftPos + 114) && CSMath.betweenInclusive(mouseY, topPos + 61, topPos + 67))
            {   graphics.setTooltipForNextFrame(Component.translatable(coolingOn ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"), mouseX, mouseY);
            }
        }
    }
}
