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
import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import net.minecraft.world.entity.player.Inventory;

public class BoilerScreen extends AbstractHearthScreen<BoilerContainer>
{
    private static final ResourceLocation BOILER_GUI = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/screen/boiler_gui.png");

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
    public void init()
    {
        super.init();
        if (particleButton != null)
        {   particleButton.setX(leftPos + 151);
            particleButton.setY(topPos + 63);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.blit(BOILER_GUI, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Draw fuel gauge
        graphics.blit(BOILER_GUI, leftPos + 109, topPos + 63, 176, 0, (int) (this.menu.getFuel() / 31.25), 14, 256, 256);

        if (!ConfigSettings.SMART_HEARTH.get() && this.menu.te.hasSmokeStack())
        {
            boolean powered = this.menu.te.isBackPowered();

            graphics.blit(BOILER_GUI, leftPos + 117, topPos + 78, 176, powered ? 14 : 22, 14, 8, 256, 256);

            if (CSMath.betweenInclusive(mouseX, leftPos + 117, leftPos + 131) && CSMath.betweenInclusive(mouseY, topPos + 78, topPos + 86))
            {   this.setTooltipForNextRenderPass(Component.translatable(powered ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"));
            }
        }
    }
}
