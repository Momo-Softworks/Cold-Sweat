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

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HearthScreen extends AbstractContainerScreen<HearthContainer>
{
    private static final ResourceLocation HEARTH_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_gui.png");
    private static final ResourceLocation RADIUS_TOGGLE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_radius_toggle.png");

    public HearthScreen(HearthContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, new TranslatableComponent("container." + ColdSweat.MOD_ID + ".hearth"));
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 176;
        this.imageHeight = 188;
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseClickedEvent.Pre event)
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
    }


    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, HEARTH_GUI);
        int x = (this.width - this.getXSize()) / 2;
        int y = (this.height - this.getYSize()) / 2;
        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int hotFuel =  (int) (this.menu.getHotFuel()  / 27.7);
        int coldFuel = (int) (this.menu.getColdFuel() / 27.7);

        // Render hot/cold fuel gauges
        blit(poseStack, leftPos + 61,  topPos + 66 - hotFuel,  176, 36 - hotFuel,  12, 36, 256, 256);
        blit(poseStack, leftPos + 103, topPos + 66 - coldFuel, 188, 36 - coldFuel, 12, 36, 256, 256);

        RenderSystem.setShaderTexture(0, RADIUS_TOGGLE);
        blit(poseStack, leftPos + 82, topPos + 68, isHoveringButton(mouseX, mouseY) ? 12 : 0, isRadiusShowing() ? 0 : 12, 12, 12, 24, 24);

        if (isHoveringButton(mouseX, mouseY))
        {
            this.renderComponentTooltip(poseStack, List.of(new TranslatableComponent("cold_sweat.screen.hearth.show_particles")), mouseX, mouseY);
        }
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
