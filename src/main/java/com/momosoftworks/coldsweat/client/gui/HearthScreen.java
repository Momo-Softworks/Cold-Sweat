package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.HearthContainer;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.DisableHearthParticlesMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Arrays;

public class HearthScreen extends DisplayEffectsScreen<HearthContainer>
{
    private static final ResourceLocation HEARTH_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_gui.png");

    public HearthScreen(HearthContainer screenContainer, PlayerInventory inv, ITextComponent title)
    {
        super(screenContainer, inv, title);
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
        this.checkEffectRendering();
        this.width = this.minecraft.getWindow().getGuiScaledWidth();
        this.height = this.minecraft.getWindow().getGuiScaledHeight();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.addButton(new ImageButton(getGuiLeft() + 82, getGuiTop() + 68, 12, 12, 176 + (!hideParticles ? 0 : 12), 36, 12, HEARTH_GUI, (button) ->
        {
            ImageButton hearthButton = (ImageButton) button;
            hideParticles = !hideParticles;
            if (hideParticles)
            {
                HearthSaveDataHandler.DISABLED_HEARTHS.add(levelPos);
                if (HearthSaveDataHandler.DISABLED_HEARTHS.size() > 20)
                {   HearthSaveDataHandler.DISABLED_HEARTHS.remove(HearthSaveDataHandler.DISABLED_HEARTHS.stream().findFirst().get());
                }
            }
            else
            {   HearthSaveDataHandler.DISABLED_HEARTHS.remove(levelPos);
            }
            Field imageX = ObfuscationReflectionHelper.findField(ImageButton.class, "field_191747_p");
            imageX.setAccessible(true);
            try
            {   imageX.set(hearthButton, 176 + (!hideParticles ? 0 : 12));
            }
            catch (Exception ignored) {}
        })
        {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button)
            {
                if (this.active && this.visible && this.isValidClickButton(button) && this.clicked(mouseX, mouseY))
                {
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.STONE_BUTTON_CLICK_ON, !hideParticles ? 1.5f : 1.9f, 0.75f));
                    this.onClick(mouseX, mouseY);
                    return true;
                }
                return false;
            }

            @Override
            public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY)
            {
                HearthScreen.this.renderWrappedToolTip(matrixStack, Arrays.asList(new TranslationTextComponent("cold_sweat.screen.hearth.show_particles")), mouseX, mouseY, font);
            }
        });
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrix);
        super.render(matrix, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrix, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        this.minecraft.textureManager.bind(HEARTH_GUI);
        int x = (this.width - this.getXSize()) / 2;
        int y = (this.height - this.getYSize()) / 2;
        this.blit(matrix, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int hotFuel  = (int) (this.menu.getHotFuel()  / 27.7);
        int coldFuel = (int) (this.menu.getColdFuel() / 27.7);

        // Render hot/cold fuel gauges
        blit(matrix, leftPos + 61,  topPos + 66 - hotFuel,  176, 36 - hotFuel,  12, hotFuel, 256, 256);
        blit(matrix, leftPos + 103, topPos + 66 - coldFuel, 188, 36 - coldFuel, 12, coldFuel, 256, 256);
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
