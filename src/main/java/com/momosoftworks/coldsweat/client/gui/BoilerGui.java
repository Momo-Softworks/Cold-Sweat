package com.momosoftworks.coldsweat.client.gui;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import com.momosoftworks.coldsweat.common.tileentity.BoilerTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class BoilerGui extends GuiContainer
{
    private static final ResourceLocation BOILER_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/boiler_gui.png");
    private final BoilerTileEntity te;

    public BoilerGui(InventoryPlayer inventory, BoilerTileEntity tileEntity)
    {   super(new BoilerContainer(inventory, tileEntity));
        this.te = tileEntity;
        this.guiLeft = 0;
        this.guiTop = 0;
        this.xSize = 176;
        this.ySize = 172;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
    {
        String title = te.getInventoryName();
        this.fontRendererObj.drawString(title, this.xSize / 2 - this.fontRendererObj.getStringWidth(title) / 2, 6, 4210752);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 94, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(BOILER_GUI);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        this.drawTexturedModalRect(guiLeft + 109, guiTop + 63, 176, 0, (int) (te.getFuel() / 31.25), 16);
    }
}
