package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.gui.config.ConfigImage;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.container.HearthContainer;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {   RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.blit(HEARTH_GUI, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int maxGaugeHeight = 14;
        int hotGaugeHeight  = this.menu.getHotFuel() <= 0 ? 0 : Math.round(CSMath.blend(2, 14, this.menu.getHotFuel(), 0, this.menu.te.getMaxFuel()));
        int coldGaugeHeight = this.menu.getColdFuel() <= 0 ? 0 : Math.round(CSMath.blend(2, 14, this.menu.getColdFuel(), 0, this.menu.te.getMaxFuel()));

        // Render hot/cold fuel gauges
        graphics.blit(HEARTH_GUI, leftPos + 62,  topPos + 49 + (maxGaugeHeight-hotGaugeHeight),  176, maxGaugeHeight - hotGaugeHeight,  14, hotGaugeHeight, 256, 256);
        graphics.blit(HEARTH_GUI, leftPos + 100, topPos + 49 + (maxGaugeHeight-coldGaugeHeight), 190, maxGaugeHeight - coldGaugeHeight, 14, coldGaugeHeight, 256, 256);

        // Render redstone indicators
        if (!ConfigSettings.SMART_HEARTH.get())
        {
            boolean coolingOn = this.menu.te.isCoolingOn();
            boolean heatingOn = this.menu.te.isHeatingOn();

            graphics.blit(HEARTH_GUI, leftPos + 63, topPos + 64, 176, heatingOn ? 28 : 32, 13, 4, 256, 256);
            graphics.blit(HEARTH_GUI, leftPos + 101, topPos + 64, 176, coolingOn ? 28 : 32, 13, 4, 256, 256);

            if (CSMath.betweenInclusive(mouseX, leftPos + 60, leftPos + 76) && CSMath.betweenInclusive(mouseY, topPos + 61, topPos + 67))
            {   this.setTooltipForNextRenderPass(Component.translatable(heatingOn ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"));
            }
            if (CSMath.betweenInclusive(mouseX, leftPos + 98, leftPos + 114) && CSMath.betweenInclusive(mouseY, topPos + 61, topPos + 67))
            {   this.setTooltipForNextRenderPass(Component.translatable(coolingOn ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"));
            }
        }
    }
}
