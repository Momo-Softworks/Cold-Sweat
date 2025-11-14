package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.container.HearthContainer;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HearthScreen extends AbstractHearthScreen<HearthContainer>
{
    private static final ResourceLocation HEARTH_GUI = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/screen/hearth_gui.png");

    @Override
    HearthBlockEntity getBlockEntity()
    {   return this.menu.te;
    }

    public HearthScreen(HearthContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, Component.translatable("container." + ColdSweat.MOD_ID + ".hearth"));
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {   RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.blit(HEARTH_GUI, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

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
            {   this.setTooltipForNextRenderPass(Component.translatable(heatingOn ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"));
            }
            if (CSMath.betweenInclusive(mouseX, leftPos + 98, leftPos + 114) && CSMath.betweenInclusive(mouseY, topPos + 61, topPos + 67))
            {   this.setTooltipForNextRenderPass(Component.translatable(coolingOn ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"));
            }
        }
    }
}
