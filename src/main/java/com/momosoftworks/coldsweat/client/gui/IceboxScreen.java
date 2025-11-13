package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.IceboxContainer;
import net.minecraft.world.entity.player.Inventory;

public class IceboxScreen extends AbstractHearthScreen<IceboxContainer>
{
    private static final ResourceLocation ICEBOX_GUI = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/screen/icebox_gui.png");

    @Override
    HearthBlockEntity getBlockEntity()
    {   return this.menu.te;
    }

    public IceboxScreen(IceboxContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 176;
        this.imageHeight = 172;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = leftPos + this.imageWidth / 2 - Minecraft.getInstance().font.width(this.getTitle()) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.blit(ICEBOX_GUI, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int maxGaugeHeight = 14;
        int gaugeHeight  = this.menu.getFuel() <= 0 ? 0 : Math.round(CSMath.blend(2, 14, this.menu.getFuel(), 0, this.menu.te.getMaxFuel()));

        // Draw fuel gauge
        graphics.blit(COLD_FUEL_GAUGE_EMPTY, leftPos + 100, topPos + 63, 0, 0, 14, 14, 14 ,14);
        graphics.blit(COLD_FUEL_GAUGE, leftPos + 100, topPos + 63 + (maxGaugeHeight-gaugeHeight), 0, maxGaugeHeight - gaugeHeight, 14, gaugeHeight, 14 ,14);

        if (!ConfigSettings.SMART_HEARTH.get() && this.menu.te.hasSmokestack())
        {
            boolean powered = this.menu.te.isCoolingOn();

            graphics.blitSprite(getPowerIndicatorSprite(powered), leftPos + 101, topPos + 78, 13, 4);

            if (CSMath.betweenInclusive(mouseX, leftPos + 98, leftPos + 117) && CSMath.betweenInclusive(mouseY, topPos + 75, topPos + 82))
            {   this.setTooltipForNextRenderPass(Component.translatable(powered ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"));
            }
        }
    }
}
