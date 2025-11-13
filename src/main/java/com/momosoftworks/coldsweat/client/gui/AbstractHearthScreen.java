package com.momosoftworks.coldsweat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.gui.config.ConfigImageButton;
import com.momosoftworks.coldsweat.client.gui.util.WidgetSprites;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.DisableHearthParticlesMessage;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public abstract class AbstractHearthScreen<T extends AbstractContainerMenu> extends EffectRenderingInventoryScreen<T>
{
    public static final WidgetSprites PARTICLES_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/particle_button_on.png"),
                                                                                      ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/particle_button_off.png"),
                                                                                      ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/particle_button_on_focus.png"),
                                                                                      ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/particle_button_off_focus.png"));
    public static final WidgetSprites POWER_INDICATOR_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/power_indicator_off.png"),
                                                                                     ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/power_indicator_on.png"));
    public static final ResourceLocation HOT_FUEL_GAUGE  = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_hot.png");
    public static final ResourceLocation HOT_FUEL_GAUGE_EMPTY = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_hot_empty.png");
    public static final ResourceLocation COLD_FUEL_GAUGE = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_cold.png");
    public static final ResourceLocation COLD_FUEL_GAUGE_EMPTY = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/sprites/hearth/fuel_gauge_cold_empty.png");

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
            particleButton = this.addRenderableWidget(new ConfigImageButton(leftPos + 160, topPos + 8, 8, 7, PARTICLES_BUTTON_SPRITES, 8, 7, (button) ->
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
                {   imageX.set(button, 176 + (!hideParticles ? 0 : 8));
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

                @Override
                public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY)
                {   AbstractHearthScreen.this.renderComponentTooltip(poseStack, List.of(Component.translatable("cold_sweat.screen.hearth.show_particles")), mouseX, mouseY, font);
                }

                @Override
                protected boolean isEnabled()
                {   return !hideParticles;
                }
            });
        }
    }

    @Override
    public void render(PoseStack ps, int mouseX, int mouseY, float partialTicks)
    {   this.renderBackground(ps);
        super.render(ps, mouseX, mouseY, partialTicks);
        this.renderTooltip(ps, mouseX, mouseY);
    }

    @Override
    public void onClose()
    {   super.onClose();
        if (this.minecraft.player != null && hideParticlesOld != hideParticles)
        {   ColdSweatPacketHandler.INSTANCE.sendToServer(new DisableHearthParticlesMessage(HearthSaveDataHandler.serializeDisabledHearths()));
        }
    }

    protected void renderFuelGauge(HearthBlockEntity.FuelType fuelType, PoseStack poseStack, int x, int y, int fuel, int maxFuel)
    {
        ResourceLocation emptyTexture = fuelType == HearthBlockEntity.FuelType.HOT ? HOT_FUEL_GAUGE_EMPTY : COLD_FUEL_GAUGE_EMPTY;
        ResourceLocation fullTexture = fuelType == HearthBlockEntity.FuelType.HOT ? HOT_FUEL_GAUGE : COLD_FUEL_GAUGE;

        int maxGaugeHeight = 14;
        int gaugeHeight  = fuel <= 0 ? 0 : Math.round(CSMath.blend(2, 14, fuel, 0, maxFuel));

        RenderSystem.setShaderTexture(0, emptyTexture);
        Screen.blit(poseStack, x, y, 0, 0, 14, 14, 14, 14);
        RenderSystem.setShaderTexture(0, fullTexture);
        Screen.blit(poseStack, x, y + (maxGaugeHeight-gaugeHeight),  0, maxGaugeHeight - gaugeHeight,  14, gaugeHeight, 14, 14);
    }

    protected void renderPowerIndicator(PoseStack poseStack, int x, int y, boolean powered)
    {
        ResourceLocation sprite = POWER_INDICATOR_SPRITES.get(true, powered);
        RenderSystem.setShaderTexture(0, sprite);
        Screen.blit(poseStack, x, y, 0, 0, 13, 4, 13, 4);
    }
}
