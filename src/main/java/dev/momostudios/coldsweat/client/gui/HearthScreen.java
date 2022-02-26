package dev.momostudios.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.container.HearthContainer;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HearthScreen extends AbstractContainerScreen<HearthContainer>
{
    private static final ResourceLocation HEARTH_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_gui.png");
    private static final ResourceLocation COLD_FUEL_GAUGE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_cold_fuel.png");
    private static final ResourceLocation HOT_FUEL_GAUGE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_hot_fuel.png");
    private static final ResourceLocation RADIUS_TOGGLE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_radius_toggle.png");
    Component name = new TranslatableComponent("block." + ColdSweat.MOD_ID + ".hearth");
    int titleX = 8;
    int coldFuelLevel;
    int hotFuelLevel;

    public HearthScreen(HearthContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.hotFuelLevel = screenContainer.te.getTileData().getInt("hot_fuel");
        this.coldFuelLevel = screenContainer.te.getTileData().getInt("cold_fuel");
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseClickedEvent event)
    {
        if (Minecraft.getInstance().screen instanceof HearthScreen screen)
        {
            if (screen.isHoveringButton(event.getMouseX(), event.getMouseY()))
            {
                boolean showRad = screen.menu.te.getTileData().getBoolean("showRadius");

                screen.menu.te.getTileData().putBoolean("showRadius", !showRad);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.STONE_BUTTON_CLICK_ON, !showRad ? 1.9f : 1.5f, 0.75f));
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);

        this.font.draw(poseStack, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
        this.font.draw(poseStack, name, titleX, 8f, 4210752);

        int hotFuel = (int) (this.menu.getHotFuel() / 27.7);
        int coldFuel = (int) (this.menu.getColdFuel() / 27.7);

        this.minecraft.textureManager.bindForSetup(HOT_FUEL_GAUGE);
        blit(poseStack, 61, 66 - hotFuel, 12, hotFuel, 0, 36 - hotFuel, 12, hotFuel, 12, 36);

        this.minecraft.textureManager.bindForSetup(COLD_FUEL_GAUGE);
        blit(poseStack, 103, 66 - coldFuel, 12, coldFuel, 0, 36 - coldFuel, 12, coldFuel, 12, 36);

        this.minecraft.textureManager.bindForSetup(RADIUS_TOGGLE);
        blit(poseStack, 82, 68, isHoveringButton(mouseX, mouseY) ? 12 : 0, isRadiusShowing() ? 0 : 12, 12, 12, 24, 24);

        if (isHoveringButton(mouseX, mouseY))
            font.draw(poseStack, "Show Particles", 97, 71, 5592405);
    }


    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        this.minecraft.textureManager.bindForSetup(HEARTH_GUI);
        int x = (this.width - this.getXSize()) / 2;
        int y = (this.height - this.getYSize()) / 2;
        this.blit(matrixStack, x ,y, 0, 0, this.getXSize(), this.getYSize());
    }

    boolean isHoveringButton(double mouseX, double mouseY)
    {
        int x = (this.width - this.getXSize()) / 2;
        int y = (this.height - this.getYSize()) / 2;

        return (mouseX >= x + 82 && mouseX <= x + 94 &&
                mouseY >= y + 68 && mouseY <= y + 80);
    }

    boolean isRadiusShowing()
    {
        return this.menu.te.getTileData().getBoolean("showRadius");
    }
}
