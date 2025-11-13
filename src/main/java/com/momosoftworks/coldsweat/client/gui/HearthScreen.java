package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.container.HearthContainer;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class HearthScreen extends AbstractHearthScreen<HearthContainer>
{
    private static final ResourceLocation HEARTH_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_gui.png");

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
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    {   RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, HEARTH_GUI);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Render hot/cold fuel gauges
        this.renderFuelGauge(HearthBlockEntity.FuelType.HOT, poseStack, leftPos + 62,  topPos + 49, this.menu.getHotFuel(), this.menu.te.getMaxFuel());
        this.renderFuelGauge(HearthBlockEntity.FuelType.COLD, poseStack, leftPos + 100, topPos + 49, this.menu.getColdFuel(), this.menu.te.getMaxFuel());

        // Render redstone indicators
        if (!ConfigSettings.SMART_HEARTH.get())
        {
            boolean coolingOn = this.menu.te.isCoolingOn();
            boolean heatingOn = this.menu.te.isHeatingOn();

            this.renderPowerIndicator(poseStack, leftPos + 63, topPos + 64, heatingOn);
            this.renderPowerIndicator(poseStack, leftPos + 101, topPos + 64, coolingOn);

            if (CSMath.betweenInclusive(mouseX, leftPos + 60, leftPos + 76) && CSMath.betweenInclusive(mouseY, topPos + 61, topPos + 67))
            {   this.renderComponentTooltip(poseStack, List.of(Component.translatable(heatingOn ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered")), mouseX, mouseY);;
            }
            if (CSMath.betweenInclusive(mouseX, leftPos + 98, leftPos + 114) && CSMath.betweenInclusive(mouseY, topPos + 61, topPos + 67))
            {   this.renderComponentTooltip(poseStack, List.of(Component.translatable(coolingOn ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered")), mouseX, mouseY);
            }
        }
    }
}
