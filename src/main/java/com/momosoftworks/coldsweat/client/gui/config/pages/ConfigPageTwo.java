package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.client.event.DrawConfigButton;
import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.ChatFormatting;
import com.momosoftworks.coldsweat.util.math.Vec2i;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;

import javax.annotation.Nullable;

public class ConfigPageTwo extends AbstractConfigPage
{
    public ConfigPageTwo(Screen parentScreen)
    {   super(parentScreen);
    }

    @Override
    public MutableComponent sectionOneTitle()
    {
        return new TranslatableComponent("cold_sweat.config.section.preference");
    }

    @Nullable
    @Override
    public MutableComponent sectionTwoTitle()
    {
        return new TranslatableComponent("cold_sweat.config.section.hud_settings");
    }

    @Override
    protected void init()
    {
        super.init();

        // Temp Offset
        this.addDecimalInput("temp_offset", Side.LEFT, new TranslatableComponent("cold_sweat.config.temp_offset.name"),
                             value -> ConfigSettings.TEMP_OFFSET.set(value.intValue()),
                             input -> input.setValue(String.valueOf(ConfigSettings.TEMP_OFFSET.get())),
                             false, false, true, new TranslatableComponent("cold_sweat.config.temp_offset.desc"));

        // Temp Smoothing
        this.addDecimalInput("temp_smoothing", Side.LEFT, new TranslatableComponent("cold_sweat.config.temp_smoothing.name"),
                             value -> ConfigSettings.TEMP_SMOOTHING.set(value),
                             input -> input.setValue(String.valueOf(ConfigSettings.TEMP_SMOOTHING.get())),
                             false, false, true, new TranslatableComponent("cold_sweat.config.temp_smoothing.desc"));

        // Hearth Debug
        this.addButton("hearth_debug", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.hearth_debug.name").append(": ").append(ConfigSettings.HEARTH_DEBUG.get() ? ON : OFF),
                button ->
                {
                    ConfigSettings.HEARTH_DEBUG.set(!ConfigSettings.HEARTH_DEBUG.get());
                },
                false, false, true, new TranslatableComponent("cold_sweat.config.hearth_debug.desc"));

        // Camera Sway
        this.addButton("camera_sway", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.distortion.name").append(": ").append(ConfigSettings.DISTORTION_EFFECTS.get() ? ON : OFF),
                button ->
                {
                    ConfigSettings.DISTORTION_EFFECTS.set(!ConfigSettings.DISTORTION_EFFECTS.get());
                },
                false, false, true, new TranslatableComponent("cold_sweat.config.distortion.desc"));

        // High Contrast
        this.addButton("high_contrast", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.high_contrast.name").append(": ").append(ConfigSettings.HIGH_CONTRAST.get() ? ON : OFF),
                button ->
                {
                    ConfigSettings.HIGH_CONTRAST.set(!ConfigSettings.HIGH_CONTRAST.get());
                },
                false, false, true, new TranslatableComponent("cold_sweat.config.high_contrast.desc"));

        // Direction Buttons: Steve Head
        this.addDirectionPanel("icon_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temp_icon_location.name"),
                amount -> ConfigSettings.BODY_ICON_POS.set(new Vec2i(ConfigSettings.BODY_ICON_POS.get().x() + amount * ConfigScreen.SHIFT_AMOUNT.get(),
                                                                   ConfigSettings.BODY_ICON_POS.get().y())),
                amount -> ConfigSettings.BODY_ICON_POS.set(new Vec2i(ConfigSettings.BODY_ICON_POS.get().x(),
                                                                     ConfigSettings.BODY_ICON_POS.get().y() + amount * ConfigScreen.SHIFT_AMOUNT.get())),
                () ->
                {   ConfigSettings.BODY_ICON_POS.set(new Vec2i(0, 0));
                },
                () ->
                {   ConfigSettings.BODY_ICON_ENABLED.set(!ConfigSettings.BODY_ICON_ENABLED.get());
                    return ConfigSettings.BODY_ICON_ENABLED.get();
                },
                false, false, true, true, new TranslatableComponent("cold_sweat.config.temp_icon_location.desc"),
                                          new TranslatableComponent("cold_sweat.config.offset_shift.name").withStyle(ChatFormatting.GRAY));

        // Direction Buttons: Temp Readout
        this.addDirectionPanel("readout_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temp_readout_location.name"),
                amount -> ConfigSettings.BODY_READOUT_POS.set(new Vec2i(ConfigSettings.BODY_READOUT_POS.get().x() + amount * ConfigScreen.SHIFT_AMOUNT.get(),
                                                                      ConfigSettings.BODY_READOUT_POS.get().y())),
                amount -> ConfigSettings.BODY_READOUT_POS.set(new Vec2i(ConfigSettings.BODY_READOUT_POS.get().x(),
                                                                      ConfigSettings.BODY_READOUT_POS.get().y() + amount * ConfigScreen.SHIFT_AMOUNT.get())),
                () ->
                {   ConfigSettings.BODY_READOUT_POS.set(new Vec2i(0, 0));
                },
                () ->
                {   ConfigSettings.BODY_READOUT_ENABLED.set(!ConfigSettings.BODY_READOUT_ENABLED.get());
                    return ConfigSettings.BODY_READOUT_ENABLED.get();
                },
                false, false, true, true, new TranslatableComponent("cold_sweat.config.temp_readout_location.desc"),
                                          new TranslatableComponent("cold_sweat.config.offset_shift.name").withStyle(ChatFormatting.GRAY));

        this.addDirectionPanel("gauge_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.world_temp_location.name"),
                amount -> ConfigSettings.WORLD_GAUGE_POS.set(new Vec2i(ConfigSettings.WORLD_GAUGE_POS.get().x() + amount * ConfigScreen.SHIFT_AMOUNT.get(),
                                                                     ConfigSettings.WORLD_GAUGE_POS.get().y())),
                amount -> ConfigSettings.WORLD_GAUGE_POS.set(new Vec2i(ConfigSettings.WORLD_GAUGE_POS.get().x(),
                                                                     ConfigSettings.WORLD_GAUGE_POS.get().y() + amount * ConfigScreen.SHIFT_AMOUNT.get())),
                () ->
                {   ConfigSettings.WORLD_GAUGE_POS.set(new Vec2i(0, 0));
                },
                () ->
                {   ConfigSettings.WORLD_GAUGE_ENABLED.set(!ConfigSettings.WORLD_GAUGE_ENABLED.get());
                    return ConfigSettings.WORLD_GAUGE_ENABLED.get();
                },
                false, false, true, true, new TranslatableComponent("cold_sweat.config.world_temp_location.desc"),
                                          new TranslatableComponent("cold_sweat.config.offset_shift.name").withStyle(ChatFormatting.GRAY));

        // Custom Hotbar
        this.addButton("custom_hotbar", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.custom_hotbar.name").append(": ").append(ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? ON : OFF),
                button -> ConfigSettings.CUSTOM_HOTBAR_LAYOUT.set(!ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get()),
                false, false, true, new TranslatableComponent("cold_sweat.config.custom_hotbar.desc"));

        // Icon Bobbing
        this.addButton("icon_bobbing", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.icon_bobbing.name").append(": ").append(ConfigSettings.ICON_BOBBING.get() ? ON : OFF),
                button -> ConfigSettings.ICON_BOBBING.set(!ConfigSettings.ICON_BOBBING.get()),
                false, false, true, new TranslatableComponent("cold_sweat.config.icon_bobbing.desc"));

        // Config Button Repositioning Screen
        this.addButton("button_position", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.config_button_pos.name"),
                       button ->
                       {
                           DrawConfigButton.EDIT_MODE = true;
                           this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
                       },
                       false, false, true, new TranslatableComponent("cold_sweat.config.config_button_pos.desc"));
    }

    @Override
    public void onClose()
    {
        super.onClose();
        ConfigScreen.saveConfig();
    }
}
