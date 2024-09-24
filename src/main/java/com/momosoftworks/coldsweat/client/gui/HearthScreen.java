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

        int hotFuel  = (int) (this.menu.getHotFuel()  / 27.7);
        int coldFuel = (int) (this.menu.getColdFuel() / 27.7);

        // Render hot/cold fuel gauges
        graphics.blit(HEARTH_GUI, leftPos + 61,  topPos + 66 - hotFuel,  176, 36 - hotFuel,  12, hotFuel, 256, 256);
        graphics.blit(HEARTH_GUI, leftPos + 103, topPos + 66 - coldFuel, 188, 36 - coldFuel, 12, coldFuel, 256, 256);

        // Render redstone indicators
        if (!ConfigSettings.SMART_HEARTH.get())
        {
            boolean sidePowered = this.menu.te.isSidePowered();
            boolean backPowered = this.menu.te.isBackPowered();

            graphics.blit(HEARTH_GUI, leftPos + 60, topPos + 21, 176, backPowered ? 60 : 68, 14, 8, 256, 256);
            graphics.blit(HEARTH_GUI, leftPos + 102, topPos + 21, 190, sidePowered ? 60 : 68, 14, 8, 256, 256);

            if (CSMath.betweenInclusive(mouseX, leftPos + 60, leftPos + 74) && CSMath.betweenInclusive(mouseY, topPos + 21, topPos + 29))
            {   this.setTooltipForNextRenderPass(Component.translatable(backPowered ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"));
            }
            if (CSMath.betweenInclusive(mouseX, leftPos + 102, leftPos + 116) && CSMath.betweenInclusive(mouseY, topPos + 21, topPos + 29))
            {   this.setTooltipForNextRenderPass(Component.translatable(sidePowered ? "gui.cold_sweat.hearth.powered" : "gui.cold_sweat.hearth.unpowered"));
            }
        }
    }
}
