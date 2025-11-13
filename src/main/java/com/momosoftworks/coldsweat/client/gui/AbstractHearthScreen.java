package com.momosoftworks.coldsweat.client.gui;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.core.network.message.DisableHearthParticlesMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class AbstractHearthScreen<T extends AbstractContainerMenu> extends EffectRenderingInventoryScreen<T>
{
    protected static final WidgetSprites PARTICLES_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/particle_button_on"),
                                                                                      ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/particle_button_off"),
                                                                                      ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/particle_button_on_focus"),
                                                                                      ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/particle_button_off_focus"));
    protected static final WidgetSprites POWER_INDICATOR_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/power_indicator_off"),
                                                                                     ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/power_indicator_on"));
    protected static final ResourceLocation HOT_FUEL_GAUGE  = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_hot.png");
    protected static final ResourceLocation HOT_FUEL_GAUGE_EMPTY = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_hot_empty.png");
    protected static final ResourceLocation COLD_FUEL_GAUGE = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_cold.png");
    protected static final ResourceLocation COLD_FUEL_GAUGE_EMPTY = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_cold_empty.png");

    ImageButton particleButton = null;
    Pair<BlockPos, ResourceLocation> levelPos = Pair.of(this.getBlockEntity().getBlockPos(), this.getBlockEntity().getLevel().dimension().location());
    boolean hideParticles = HearthSaveDataHandler.DISABLED_HEARTHS.contains(levelPos);
    boolean hideParticlesOld = hideParticles;

    abstract HearthBlockEntity getBlockEntity();

    public AbstractHearthScreen(T screenContainer, Inventory inv, Component title)
    {   super(screenContainer, inv, title);
    }

    @Override
    public void init()
    {   super.init();
        if (this.getBlockEntity().hasSmokestack())
        {
            particleButton = this.addRenderableWidget(new ImageButton(leftPos + 160, topPos + 8, 8, 7, PARTICLES_BUTTON_SPRITES, (button) ->
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
            })
            {
                @Override
                public boolean mouseClicked(double mouseX, double mouseY, int button)
                {
                    if (this.active && this.visible && this.isValidClickButton(button) && this.clicked(mouseX, mouseY))
                    {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.STONE_BUTTON_CLICK_ON, !hideParticles ? 1.5f : 1.9f, 0.75f));
                        this.onClick(mouseX, mouseY);
                        this.setFocused(false);
                        return true;
                    }
                    return false;
                }

                @Override
                public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
                {
                    ResourceLocation resourcelocation = this.sprites.get(!hideParticles, this.isHoveredOrFocused());
                    guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), this.width, this.height);
                }
            });
            particleButton.setTooltip(Tooltip.create(Component.translatable("cold_sweat.screen.hearth.show_particles")));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {   super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
        this.children().forEach(child -> child.setFocused(false));
    }

    @Override
    public void onClose()
    {   super.onClose();
        if (this.minecraft.player != null && hideParticlesOld != hideParticles)
        {   PacketDistributor.sendToServer(new DisableHearthParticlesMessage(HearthSaveDataHandler.serializeDisabledHearths()));
        }
    }

    protected static ResourceLocation getPowerIndicatorSprite(boolean powered)
    {   return POWER_INDICATOR_SPRITES.get(true, powered);
    }
}
