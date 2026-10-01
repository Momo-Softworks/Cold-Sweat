package com.momosoftworks.coldsweat.client.gui;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.core.network.message.DisableHearthParticlesMessage;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class AbstractHearthScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
{
    public static final WidgetSprites PARTICLES_BUTTON_SPRITES = new WidgetSprites(Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/particle_button_on"),
                                                                                      Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/particle_button_off"),
                                                                                      Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/particle_button_on_focus"),
                                                                                      Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/particle_button_off_focus"));
    public static final WidgetSprites POWER_INDICATOR_SPRITES = new WidgetSprites(Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/power_indicator_off"),
                                                                                     Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth/power_indicator_on"));
    public static final Identifier HOT_FUEL_GAUGE  = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_hot.png");
    public static final Identifier HOT_FUEL_GAUGE_EMPTY = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_hot_empty.png");
    public static final Identifier COLD_FUEL_GAUGE = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_cold.png");
    public static final Identifier COLD_FUEL_GAUGE_EMPTY = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_cold_empty.png");

    ImageButton particleButton = null;
    Pair<BlockPos, Identifier> levelPos = Pair.of(this.getBlockEntity().getBlockPos(), this.getBlockEntity().getLevel().dimension().identifier());
    boolean hideParticles = HearthSaveDataHandler.DISABLED_HEARTHS.contains(levelPos);
    boolean hideParticlesOld = hideParticles;

    EffectsInInventory effects;

    abstract HearthBlockEntity getBlockEntity();

    public AbstractHearthScreen(T screenContainer, Inventory inv, Component title, int imageWidth, int imageHeight)
    {   super(screenContainer, inv, title, imageWidth, imageHeight);
    }

    @Override
    public void init()
    {   super.init();
        this.effects = new EffectsInInventory(this);
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
                public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
                {
                    if (this.active && this.visible && this.isValidClickButton(event.buttonInfo()) && this.isMouseOver(event.x(), event.y()))
                    {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.STONE_BUTTON_CLICK_ON, !hideParticles ? 1.5f : 1.9f, 0.75f));
                        this.onClick(event, doubleClick);
                        this.setFocused(false);
                        return true;
                    }
                    return false;
                }

                @Override
                public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
                {
                    Identifier resourcelocation = this.sprites.get(!hideParticles, this.isHoveredOrFocused());
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourcelocation, this.getX(), this.getY(), this.width, this.height);
                }
            });
            particleButton.setTooltip(Tooltip.create(Component.translatable("cold_sweat.screen.hearth.show_particles")));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {   this.effects.extractRenderState(graphics, mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.children().forEach(child -> child.setFocused(false));
    }

    @Override
    public boolean showsActiveEffects()
    {   return this.effects.canSeeEffects();
    }

    @Override
    public void onClose()
    {   super.onClose();
        if (this.minecraft.player != null && hideParticlesOld != hideParticles)
        {   ClientPacketDistributor.sendToServer(new DisableHearthParticlesMessage(HearthSaveDataHandler.serializeDisabledHearths()));
        }
    }

    protected void renderFuelGauge(HearthBlockEntity.FuelType fuelType, GuiGraphicsExtractor graphics, int x, int y, int fuel, int maxFuel)
    {
        Identifier emptyTexture = fuelType == HearthBlockEntity.FuelType.HOT ? HOT_FUEL_GAUGE_EMPTY : COLD_FUEL_GAUGE_EMPTY;
        Identifier fullTexture = fuelType == HearthBlockEntity.FuelType.HOT ? HOT_FUEL_GAUGE : COLD_FUEL_GAUGE;

        int maxGaugeHeight = 14;
        int gaugeHeight  = fuel <= 0 ? 0 : Math.round(CSMath.blend(2, 14, fuel, 0, maxFuel));

        graphics.blit(RenderPipelines.GUI_TEXTURED, emptyTexture, x, y, 0, 0, 14, 14, 14, 14);
        graphics.blit(RenderPipelines.GUI_TEXTURED, fullTexture, x, y + (maxGaugeHeight-gaugeHeight), 0, maxGaugeHeight - gaugeHeight, 14, gaugeHeight, 14, 14);
    }

    protected void renderPowerIndicator(GuiGraphicsExtractor graphics, int x, int y, boolean powered)
    {
        Identifier sprite = POWER_INDICATOR_SPRITES.get(true, powered);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 13, 4);
    }
}
