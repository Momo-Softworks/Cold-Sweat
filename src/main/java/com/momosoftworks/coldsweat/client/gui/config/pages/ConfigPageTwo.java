package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.client.event.DrawConfigButton;
import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class ConfigPageTwo extends AbstractConfigPage
{
    private final String ON;
    private final String OFF;

    public ConfigPageTwo(Screen parentScreen)
    {
        super(parentScreen);
        ON = DialogTexts.OPTION_ON.getString();
        OFF = DialogTexts.OPTION_OFF.getString();
    }

    @Override
    public int index()
    {
        return 1;
    }

    @Override
    public ITextComponent sectionOneTitle()
    {
        return new TranslationTextComponent("cold_sweat.config.section.other");
    }

    @Nullable
    @Override
    public ITextComponent sectionTwoTitle()
    {
        return new TranslationTextComponent("cold_sweat.config.section.hud_settings");
    }

    @Override
    protected void init()
    {
        super.init();

        ClientSettingsConfig clientConfig = ClientSettingsConfig.getInstance();

        // Temp Offset
        this.addDecimalInput("temp_offset", Side.LEFT, new TranslationTextComponent("cold_sweat.config.temp_offset.name"),
                             value -> clientConfig.setTempOffset(value.intValue()),
                             input -> input.setValue(String.valueOf(clientConfig.getTempOffset())),
                             false, false, true, new TranslationTextComponent("cold_sweat.config.temp_offset.desc").getString()+"§r");

        // Enable Grace Period
        this.addButton("grace_toggle", Side.LEFT, () -> new TranslationTextComponent("cold_sweat.config.grace_period.name").getString() + ": " + (ConfigSettings.GRACE_ENABLED.get() ? ON : OFF),
                button ->
                {
                    ConfigSettings.GRACE_ENABLED.set(!ConfigSettings.GRACE_ENABLED.get());

                    button.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.grace_period.name").getString() + ": "
                            + (ConfigSettings.GRACE_ENABLED.get() ? ON : OFF)));
                },
                true, false, false, new TranslationTextComponent("cold_sweat.config.grace_period.desc").getString());

        // Grace Period Length
        this.addDecimalInput("grace_length", Side.LEFT, new TranslationTextComponent("cold_sweat.config.grace_period_length.name"),
                value -> ConfigSettings.GRACE_LENGTH.set(value.intValue()),
                input -> input.setValue(ConfigSettings.GRACE_LENGTH.get() + ""),
                true, false, false, new TranslationTextComponent("cold_sweat.config.grace_period_length.desc_1").getString(),
                            "§7"+new TranslationTextComponent("cold_sweat.config.grace_period_length.desc_2").getString()+"§r");

        // Hearth Debug
        this.addButton("hearth_debug", Side.LEFT, () -> new TranslationTextComponent("cold_sweat.config.hearth_debug.name").getString()
                        + ": " + (clientConfig.isHearthDebugEnabled() ? ON : OFF),
                button ->
                {
                    clientConfig.setHearthDebug(!clientConfig.isHearthDebugEnabled());
                },
                false, false, true, new TranslationTextComponent("cold_sweat.config.hearth_debug.desc").getString());

        // Camera Sway
        this.addButton("camera_sway", Side.LEFT, () -> new TranslationTextComponent("cold_sweat.config.distortion.name").getString()
                        + ": " + (clientConfig.areDistortionsEnabled() ? ON : OFF),
                button ->
                {
                    clientConfig.setDistortionsEnabled(!clientConfig.areDistortionsEnabled());
                },
                false, false, true, new TranslationTextComponent("cold_sweat.config.distortion.desc").getString());

        // High Contrast
        this.addButton("high_contrast", Side.LEFT, () -> new TranslationTextComponent("cold_sweat.config.high_contrast.name").getString()
                               + ": " + (clientConfig.isHighContrast() ? ON : OFF),
                       button ->
                       {
                           clientConfig.setHighContrast(!clientConfig.isHighContrast());
                       },
                       false, false, true, new TranslationTextComponent("cold_sweat.config.high_contrast.desc").getString());

        // Direction Buttons: Steve Head
        this.addDirectionPanel("icon_directions", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.temp_icon_location.name"),
                amount -> clientConfig.setBodyIconX(clientConfig.getBodyIconX() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                amount -> clientConfig.setBodyIconY(clientConfig.getBodyIconY() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                () -> { clientConfig.setBodyIconX(0); clientConfig.setBodyIconY(0); },
                false, false, true, new TranslationTextComponent("cold_sweat.config.temp_icon_location.desc").getString(),
                           "§7"+new TranslationTextComponent("cold_sweat.config.offset_shift.name").getString());

        // Direction Buttons: Temp Readout
        this.addDirectionPanel("readout_directions", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.temp_readout_location.name"),
                amount -> clientConfig.setBodyReadoutX(clientConfig.getBodyReadoutX() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                amount -> clientConfig.setBodyReadoutY(clientConfig.getBodyReadoutY() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                () -> { clientConfig.setBodyReadoutX(0); clientConfig.setBodyReadoutY(0); },
                false, false, true, new TranslationTextComponent("cold_sweat.config.temp_readout_location.desc").getString(),
                           "§7"+new TranslationTextComponent("cold_sweat.config.offset_shift.name").getString());

        this.addDirectionPanel("gauge_directions", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.world_temp_location.name"),
                amount -> clientConfig.setWorldGaugeX(clientConfig.getWorldGaugeX() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                amount -> clientConfig.setWorldGaugeY(clientConfig.getWorldGaugeY() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                () -> { clientConfig.setWorldGaugeX(0); clientConfig.setWorldGaugeY(0); },
                false, false, true, new TranslationTextComponent("cold_sweat.config.world_temp_location.desc").getString(),
                           "§7"+new TranslationTextComponent("cold_sweat.config.offset_shift.name").getString());

        // Custom Hotbar
        this.addButton("custom_hotbar", Side.RIGHT, () -> new TranslationTextComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " + (clientConfig.customHotbarEnabled() ? ON : OFF),
                button -> clientConfig.setCustomHotbar(!clientConfig.customHotbarEnabled()),
                false, false, true, new TranslationTextComponent("cold_sweat.config.custom_hotbar.desc").getString());

        // Icon Bobbing
        this.addButton("icon_bobbing", Side.RIGHT, () -> new TranslationTextComponent("cold_sweat.config.icon_bobbing.name").getString() + ": " + (clientConfig.isIconBobbingEnabled() ? ON : OFF),
                button -> clientConfig.setIconBobbing(!clientConfig.isIconBobbingEnabled()),
                false, false, true, new TranslationTextComponent("cold_sweat.config.icon_bobbing.desc").getString());

        // Config Button Repositioning Screen
        this.addButton("button_position", Side.RIGHT, () -> new TranslationTextComponent("cold_sweat.config.config_button_pos.name").getString(),
                       button ->
                       {
                           DrawConfigButton.DRAW_CONTROLS = true;
                           this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
                       },
                       false, false, true, new TranslationTextComponent("cold_sweat.config.config_button_pos.desc").getString());
    }

    @Override
    public void onClose()
    {
        super.onClose();
        ConfigScreen.saveConfig();
    }
}
