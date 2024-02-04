package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.client.event.DrawConfigButton;
import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class ConfigPageTwo extends AbstractConfigPage
{
    private final String ON;
    private final String OFF;

    public ConfigPageTwo(Screen parentScreen)
    {
        super(parentScreen);
        ON = CommonComponents.OPTION_ON.getString();
        OFF = CommonComponents.OPTION_OFF.getString();
    }

    @Override
    public int index()
    {
        return 1;
    }

    @Override
    public BaseComponent sectionOneTitle()
    {
        return new TranslatableComponent("cold_sweat.config.section.other");
    }

    @Nullable
    @Override
    public BaseComponent sectionTwoTitle()
    {
        return new TranslatableComponent("cold_sweat.config.section.hud_settings");
    }

    @Override
    protected void init()
    {
        super.init();

        ClientSettingsConfig clientConfig = ClientSettingsConfig.getInstance();

        // Temp Offset
        this.addDecimalInput("temp_offset", Side.LEFT, new TranslatableComponent("cold_sweat.config.temp_offset.name"),
                             value -> clientConfig.setTempOffset(value.intValue()),
                             input -> input.setValue(String.valueOf(clientConfig.getTempOffset())),
                             false, false, true, new TranslatableComponent("cold_sweat.config.temp_offset.desc"));

        // Enable Grace Period
        this.addButton("grace_toggle", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.grace_period.name").append(": ").append(ConfigSettings.GRACE_ENABLED.get() ? ON : OFF),
                button ->
                {
                    ConfigSettings.GRACE_ENABLED.set(!ConfigSettings.GRACE_ENABLED.get());

                    button.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.grace_period.name").getString() + ": "
                            + (ConfigSettings.GRACE_ENABLED.get() ? ON : OFF)));
                },
                true, false, false, new TranslatableComponent("cold_sweat.config.grace_period.desc"));

        // Grace Period Length
        this.addDecimalInput("grace_length", Side.LEFT, new TranslatableComponent("cold_sweat.config.grace_period_length.name"),
                value -> ConfigSettings.GRACE_LENGTH.set(value.intValue()),
                input -> input.setValue(ConfigSettings.GRACE_LENGTH.get() + ""),
                true, false, false, new TranslatableComponent("cold_sweat.config.grace_period_length.desc_1"),
                            new TranslatableComponent("cold_sweat.config.grace_period_length.desc_2").withStyle(ChatFormatting.DARK_GRAY));

        // Hearth Debug
        this.addButton("hearth_debug", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.hearth_debug.name").append(": ").append(clientConfig.isHearthDebugEnabled() ? ON : OFF),
                button ->
                {
                    clientConfig.setHearthDebug(!clientConfig.isHearthDebugEnabled());
                },
                false, false, true, new TranslatableComponent("cold_sweat.config.hearth_debug.desc"));

        // Camera Sway
        this.addButton("camera_sway", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.distortion.name").append(": ").append(clientConfig.areDistortionsEnabled() ? ON : OFF),
                button ->
                {
                    clientConfig.setDistortionsEnabled(!clientConfig.areDistortionsEnabled());
                },
                false, false, true, new TranslatableComponent("cold_sweat.config.distortion.desc"));

        // High Contrast
        this.addButton("high_contrast", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.high_contrast.name").append(": ").append(clientConfig.isHighContrast() ? ON : OFF),
                button ->
                {
                    clientConfig.setHighContrast(!clientConfig.isHighContrast());
                },
                false, false, true, new TranslatableComponent("cold_sweat.config.high_contrast.desc"));

        // Direction Buttons: Steve Head
        this.addDirectionPanel("icon_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temp_icon_location.name"),
                amount -> clientConfig.setBodyIconX(clientConfig.getBodyIconX() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                amount -> clientConfig.setBodyIconY(clientConfig.getBodyIconY() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                () ->
                {   clientConfig.setBodyIconX(0);
                    clientConfig.setBodyIconY(0);
                },
                () ->
                {   clientConfig.setBodyIconEnabled(!clientConfig.isBodyIconEnabled());
                    return clientConfig.isBodyIconEnabled();
                },
                false, false, true, true, new TranslatableComponent("cold_sweat.config.temp_icon_location.desc"),
                           new TranslatableComponent("cold_sweat.config.offset_shift.name"));

        // Direction Buttons: Temp Readout
        this.addDirectionPanel("readout_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temp_readout_location.name"),
                amount -> clientConfig.setBodyReadoutX(clientConfig.getBodyReadoutX() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                amount -> clientConfig.setBodyReadoutY(clientConfig.getBodyReadoutY() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                () ->
                {   clientConfig.setBodyReadoutX(0);
                    clientConfig.setBodyReadoutY(0);
                },
                () ->
                {   clientConfig.setBodyReadoutEnabled(!clientConfig.isBodyReadoutEnabled());
                    return clientConfig.isBodyReadoutEnabled();
                },
                false, false, true, true, new TranslatableComponent("cold_sweat.config.temp_readout_location.desc"),
                           new TranslatableComponent("cold_sweat.config.offset_shift.name"));

        this.addDirectionPanel("gauge_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.world_temp_location.name"),
                amount -> clientConfig.setWorldGaugeX(clientConfig.getWorldGaugeX() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                amount -> clientConfig.setWorldGaugeY(clientConfig.getWorldGaugeY() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                () ->
                {   clientConfig.setWorldGaugeX(0);
                    clientConfig.setWorldGaugeY(0);
                },
                () ->
                {   clientConfig.setWorldGaugeEnabled(!clientConfig.isWorldGaugeEnabled());
                    return clientConfig.isWorldGaugeEnabled();
                },
                false, false, true, true, new TranslatableComponent("cold_sweat.config.world_temp_location.desc"),
                           new TranslatableComponent("cold_sweat.config.offset_shift.name"));

        // Custom Hotbar
        this.addButton("custom_hotbar", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.custom_hotbar.name").append(": ").append(clientConfig.customHotbarEnabled() ? ON : OFF),
                button -> clientConfig.setCustomHotbar(!clientConfig.customHotbarEnabled()),
                false, false, true, new TranslatableComponent("cold_sweat.config.custom_hotbar.desc"));

        // Icon Bobbing
        this.addButton("icon_bobbing", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.icon_bobbing.name").append(": ").append(clientConfig.isIconBobbingEnabled() ? ON : OFF),
                button -> clientConfig.setIconBobbing(!clientConfig.isIconBobbingEnabled()),
                false, false, true, new TranslatableComponent("cold_sweat.config.icon_bobbing.desc"));

        // Config Button Repositioning Screen
        this.addButton("button_position", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.config_button_pos.name"),
                       button ->
                       {
                           DrawConfigButton.DRAW_CONTROLS = true;
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
