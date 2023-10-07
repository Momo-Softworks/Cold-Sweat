package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.HearthContainer;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.DisableHearthParticlesMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class HearthScreen extends EffectRenderingInventoryScreen<HearthContainer>
{
    private static final ResourceLocation HEARTH_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_gui.png");

    public HearthScreen(HearthContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, Component.translatable("container." + ColdSweat.MOD_ID + ".hearth"));
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    Pair<BlockPos, ResourceLocation> levelPos = Pair.of(this.menu.te.getBlockPos(), this.menu.te.getLevel().dimension().location());
    boolean hideParticles = HearthSaveDataHandler.DISABLED_HEARTHS.contains(levelPos);
    boolean hideParticlesOld = hideParticles;

    @Override
    public void init()
    {
        super.init();
        ImageButton particleButton = this.addRenderableWidget(new ImageButton(leftPos + 82, topPos + 68, 12, 12, 176 + (!hideParticles ? 0 : 12), 36, 12, HEARTH_GUI, (button) ->
        {
            hideParticles = !hideParticles;
            // If particles are disabled, add the hearth to the list of disabled hearths
            if (hideParticles)
            {
                HearthSaveDataHandler.DISABLED_HEARTHS.add(levelPos);
                // Limit the number of disabled hearths to 64
                if (HearthSaveDataHandler.DISABLED_HEARTHS.size() > 64)
                {   HearthSaveDataHandler.DISABLED_HEARTHS.remove(HearthSaveDataHandler.DISABLED_HEARTHS.iterator().next());
                }
            }
            // Otherwise, remove it from the list
            else
            {   HearthSaveDataHandler.DISABLED_HEARTHS.remove(levelPos);
            }

            Field imageX = ObfuscationReflectionHelper.findField(ImageButton.class, "f_94224_");
            imageX.setAccessible(true);
            try
            {   imageX.set(button, 176 + (!hideParticles ? 0 : 12));
            }
            catch (Exception ignored) {}
        })
        {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button)
            {
                if (this.active && this.visible && this.isValidClickButton(button) && this.clicked(mouseX, mouseY))
                {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.STONE_BUTTON_CLICK_ON, !hideParticles ? 1.5f : 1.9f, 0.75f));
                    this.onClick(mouseX, mouseY);
                    return true;
                }
                return false;
            }
        });
        particleButton.setTooltip(Tooltip.create(Component.translatable("cold_sweat.screen.hearth.show_particles")));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {   this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int x = (this.width - this.getXSize()) / 2;
        int y = (this.height - this.getYSize()) / 2;
        graphics.blit(HEARTH_GUI, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int hotFuel  = (int) (this.menu.getHotFuel()  / 27.7);
        int coldFuel = (int) (this.menu.getColdFuel() / 27.7);

        // Render hot/cold fuel gauges
        graphics.blit(HEARTH_GUI, leftPos + 61,  topPos + 66 - hotFuel,  176, 36 - hotFuel,  12, hotFuel, 256, 256);
        graphics.blit(HEARTH_GUI, leftPos + 103, topPos + 66 - coldFuel, 188, 36 - coldFuel, 12, coldFuel, 256, 256);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        if (this.minecraft.player != null && hideParticlesOld != hideParticles)
        {
            ColdSweatPacketHandler.INSTANCE.sendToServer(new DisableHearthParticlesMessage(Minecraft.getInstance().player, HearthSaveDataHandler.serializeDisabledHearths()));
        }
    }
}
