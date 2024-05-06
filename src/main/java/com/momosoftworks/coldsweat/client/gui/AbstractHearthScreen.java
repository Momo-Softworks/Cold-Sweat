package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.DisableHearthParticlesMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.NetworkDirection;

import java.lang.reflect.Field;
import java.util.Arrays;

public abstract class AbstractHearthScreen<T extends Container> extends DisplayEffectsScreen<T>
{
    private static final ResourceLocation HEARTH_GUI = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/screen/hearth_gui.png");

    ImageButton particleButton = null;
    Pair<BlockPos, ResourceLocation> levelPos = Pair.of(this.getBlockEntity().getBlockPos(), this.getBlockEntity().getLevel().dimension().location());
    boolean hideParticles = HearthSaveDataHandler.DISABLED_HEARTHS.contains(levelPos);
    boolean hideParticlesOld = hideParticles;

    abstract HearthBlockEntity getBlockEntity();

    public AbstractHearthScreen(T screenContainer, PlayerInventory inv, ITextComponent title)
    {   super(screenContainer, inv, title);
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void init()
    {   super.init();
        if (this.getBlockEntity().hasSmokeStack())
        {
            particleButton = this.addWidget(new ImageButton(leftPos + 82, topPos + 68, 12, 12, 176 + (!hideParticles ? 0 : 12), 36, 12, HEARTH_GUI, (button) ->
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
                        Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.STONE_BUTTON_CLICK_ON, !hideParticles ? 1.5f : 1.9f, 0.75f));
                        this.onClick(mouseX, mouseY);
                        this.setFocused(false);
                        return true;
                    }
                    return false;
                }

                @Override
                public void renderToolTip(MatrixStack poseStack, int mouseX, int mouseY)
                {   AbstractHearthScreen.this.renderComponentTooltip(poseStack, Arrays.asList(new TranslationTextComponent("cold_sweat.screen.hearth.show_particles")), mouseX, mouseY);
                }
            });
        }
    }

    @Override
    public void render(MatrixStack ps, int mouseX, int mouseY, float partialTicks)
    {   this.renderBackground(ps);
        super.render(ps, mouseX, mouseY, partialTicks);
        this.renderTooltip(ps, mouseX, mouseY);
        this.children().forEach(child -> child.changeFocus(false));
    }

    @Override
    public void onClose()
    {   super.onClose();
        if (this.minecraft.player != null && hideParticlesOld != hideParticles)
        {   ColdSweatPacketHandler.INSTANCE.sendTo(new DisableHearthParticlesMessage(HearthSaveDataHandler.serializeDisabledHearths()), Minecraft.getInstance().getConnection().getConnection(), NetworkDirection.PLAY_TO_SERVER);
        }
    }
}
