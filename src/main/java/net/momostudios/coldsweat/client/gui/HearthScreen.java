package net.momostudios.coldsweat.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.container.HearthContainer;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HearthScreen extends ContainerScreen<HearthContainer>
{
    private static final ResourceLocation HEARTH_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_gui.png");
    private static final ResourceLocation COLD_FUEL_GAUGE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_cold_fuel.png");
    private static final ResourceLocation HOT_FUEL_GAUGE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_hot_fuel.png");
    private static final ResourceLocation RADIUS_TOGGLE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_radius_toggle.png");
    ITextComponent name = new TranslationTextComponent("block." + ColdSweat.MOD_ID + ".hearth");
    int titleX = 8;
    int coldFuelLevel;
    int hotFuelLevel;

    public HearthScreen(HearthContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.guiLeft = 0;
        this.guiTop = 0;
        this.xSize = 176;
        this.ySize = 166;
        this.hotFuelLevel = screenContainer.te.getTileData().getInt("hot_fuel");
        this.coldFuelLevel = screenContainer.te.getTileData().getInt("cold_fuel");
    }

    @SubscribeEvent
    public static void onMouseClick(GuiScreenEvent.MouseClickedEvent event)
    {
        if (Minecraft.getInstance().currentScreen instanceof HearthScreen)
        {
            HearthScreen screen = ((HearthScreen) Minecraft.getInstance().currentScreen);
            if (screen.isHoveringButton(event.getMouseX(), event.getMouseY()))
            {
                boolean showRad = screen.container.te.getTileData().getBoolean("showRadius");

                screen.container.te.getTileData().putBoolean("showRadius", !showRad);
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(new SoundEvent(
                        new ResourceLocation("minecraft:block.stone_button.click_on")), !showRad ? 1.9f : 1.5f, 0.75f));
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y)
    {
        this.font.drawText(matrixStack, this.playerInventory.getDisplayName(), (float) this.playerInventoryTitleX, (float) this.playerInventoryTitleY, 4210752);
        this.font.drawText(matrixStack, name, titleX, 8f, 4210752);

        int hotFuel = (int) (this.container.getHotFuel() / 27.7);
        int coldFuel = (int) (this.container.getColdFuel() / 27.7);

        this.minecraft.textureManager.bindTexture(HOT_FUEL_GAUGE);
        blit(matrixStack, 61, 66 - hotFuel, 12, hotFuel, 0, 36 - hotFuel, 12, hotFuel, 12, 36);

        this.minecraft.textureManager.bindTexture(COLD_FUEL_GAUGE);
        blit(matrixStack, 103, 66 - coldFuel, 12, coldFuel, 0, 36 - coldFuel, 12, coldFuel, 12, 36);

        this.minecraft.textureManager.bindTexture(RADIUS_TOGGLE);
        blit(matrixStack, 82, 68, isHoveringButton(x, y) ? 12 : 0, isRadiusShowing() ? 0 : 12, 12, 12, 24, 24);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        this.minecraft.textureManager.bindTexture(HEARTH_GUI);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(matrixStack, x ,y, 0, 0, this.xSize, this.ySize);
    }

    boolean isHoveringButton(double mouseX, double mouseY)
    {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        return (mouseX >= x + 82 && mouseX <= x + 94 &&
                mouseY >= y + 68 && mouseY <= y + 80);
    }

    boolean isRadiusShowing()
    {
        return this.container.te.getTileData().getBoolean("showRadius");
    }
}
