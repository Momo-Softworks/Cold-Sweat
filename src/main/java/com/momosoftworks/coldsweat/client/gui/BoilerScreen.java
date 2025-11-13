package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class BoilerScreen extends AbstractHearthScreen<BoilerContainer>
{
    public static final ResourceLocation BOILER_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/boiler_gui.png");

    @Override
    HearthBlockEntity getBlockEntity()
    {   return this.menu.te;
    }

    public BoilerScreen(BoilerContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 176;
        this.imageHeight = 172;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = leftPos + this.imageWidth / 2 - Minecraft.getInstance().font.width(this.getTitle()) / 2;
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, BOILER_GUI);
        this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Render hot/cold fuel gauges
        this.renderFuelGauge(HearthBlockEntity.FuelType.HOT, matrixStack, leftPos + 100,  topPos + 63, this.menu.getFuel(), this.menu.te.getMaxFuel());

        if (!ConfigSettings.SMART_HEARTH.get() && this.menu.te.hasSmokestack())
        {
            boolean powered = this.menu.te.isHeatingOn();

            this.renderPowerIndicator(matrixStack, leftPos + 101, topPos + 78, powered);

            if (CSMath.betweenInclusive(mouseX, leftPos + 98, leftPos + 117) && CSMath.betweenInclusive(mouseY, topPos + 75, topPos + 82))
            {   this.renderComponentTooltip(matrixStack, List.of(Component.translatable(powered ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered")), mouseX, mouseY);
            }
        }
    }
}
