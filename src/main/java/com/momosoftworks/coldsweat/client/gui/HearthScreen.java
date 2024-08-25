package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
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
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Arrays;

public class HearthScreen extends AbstractHearthScreen<HearthContainer>
{
    private static final ResourceLocation HEARTH_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_gui.png");

    @Override
    HearthBlockEntity getBlockEntity()
    {   return this.menu.te;
    }

    public HearthScreen(HearthContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, new TranslatableComponent("container." + ColdSweat.MOD_ID + ".hearth"));
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(MatrixStack poseStack, float partialTicks, int mouseX, int mouseY)
    {   RenderSystem.color4f(1f, 1f, 1f, 1f);
        this.minecraft.textureManager.bind(HEARTH_GUI);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int hotFuel  = (int) (this.menu.getHotFuel()  / 27.7);
        int coldFuel = (int) (this.menu.getColdFuel() / 27.7);

        // Render hot/cold fuel gauges
        blit(poseStack, leftPos + 61,  topPos + 66 - hotFuel,  176, 36 - hotFuel,  12, hotFuel, 256, 256);
        blit(poseStack, leftPos + 103, topPos + 66 - coldFuel, 188, 36 - coldFuel, 12, coldFuel, 256, 256);
    }
}
