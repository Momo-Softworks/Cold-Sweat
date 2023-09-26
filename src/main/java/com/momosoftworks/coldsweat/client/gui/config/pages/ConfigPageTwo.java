package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.client.event.DrawConfigButton;
import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

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
    public Component sectionOneTitle()
    {
        return Component.translatable("cold_sweat.config.section.other");
    }

    @Nullable
    @Override
    public Component sectionTwoTitle()
    {
        return Component.translatable("cold_sweat.config.section.hud_settings");
    }

    @Override
    protected void init()
    {
        super.init();

        ClientSettingsConfig clientConfig = ClientSettingsConfig.getInstance();

        // Enable Grace Period
        this.addButton("grace_toggle", Side.LEFT, () -> Component.translatable("cold_sweat.config.grace_period.name").getString() + ": " + (ConfigSettings.GRACE_ENABLED.get() ? ON : OFF),
                button ->
                {
                    ConfigSettings.GRACE_ENABLED.set(!ConfigSettings.GRACE_ENABLED.get());

                    button.setMessage(Component.literal(Component.translatable("cold_sweat.config.grace_period.name").getString() + ": "
                            + (ConfigSettings.GRACE_ENABLED.get() ? ON : OFF)));
                },
                true, false, false, Component.translatable("cold_sweat.config.grace_period.desc").getString());

        // Grace Period Length
        this.addDecimalInput("grace_length", Side.LEFT, Component.translatable("cold_sweat.config.grace_period_length.name"),
                value -> ConfigSettings.GRACE_LENGTH.set(value.intValue()),
                input -> input.setValue(ConfigSettings.GRACE_LENGTH.get() + ""),
                true, false, false, Component.translatable("cold_sweat.config.grace_period_length.desc_1").getString(),
                            "§7"+Component.translatable("cold_sweat.config.grace_period_length.desc_2").getString()+"§r");

        // Hearth Debug
        this.addButton("hearth_debug", Side.LEFT, () -> Component.translatable("cold_sweat.config.hearth_debug.name").getString()
                        + ": " + (clientConfig.isHearthDebugEnabled() ? ON : OFF),
                button ->
                {
                    clientConfig.setHearthDebug(!clientConfig.isHearthDebugEnabled());
                },
                false, false, true, Component.translatable("cold_sweat.config.hearth_debug.desc").getString());

        // Camera Sway
        this.addButton("camera_sway", Side.LEFT, () -> Component.translatable("cold_sweat.config.distortion.name").getString()
                        + ": " + (clientConfig.areDistortionsEnabled() ? ON : OFF),
                button ->
                {
                    clientConfig.setDistortionsEnabled(!clientConfig.areDistortionsEnabled());
                },
                false, false, true, Component.translatable("cold_sweat.config.distortion.desc").getString());

        // Direction Buttons: Steve Head
        this.addDirectionPanel("icon_directions", Side.RIGHT, Component.translatable("cold_sweat.config.temp_icon_location.name"),
                amount -> clientConfig.setBodyIconX(clientConfig.getBodyIconX() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                amount -> clientConfig.setBodyIconY(clientConfig.getBodyIconY() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                () -> { clientConfig.setBodyIconX(0); clientConfig.setBodyIconY(0); },
                false, false, true, Component.translatable("cold_sweat.config.temp_icon_location.desc").getString(),
                           "§7"+Component.translatable("cold_sweat.config.offset_shift.name").getString());

        // Direction Buttons: Temp Readout
        this.addDirectionPanel("readout_directions", Side.RIGHT, Component.translatable("cold_sweat.config.temp_readout_location.name"),
                amount -> clientConfig.setBodyReadoutX(clientConfig.getBodyReadoutX() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                amount -> clientConfig.setBodyReadoutY(clientConfig.getBodyReadoutY() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                () -> { clientConfig.setBodyReadoutX(0); clientConfig.setBodyReadoutY(0); },
                false, false, true, Component.translatable("cold_sweat.config.temp_readout_location.desc").getString(),
                           "§7"+Component.translatable("cold_sweat.config.offset_shift.name").getString());

        this.addDirectionPanel("gauge_directions", Side.RIGHT, Component.translatable("cold_sweat.config.world_temp_location.name"),
                amount -> clientConfig.setWorldGaugeX(clientConfig.getWorldGaugeX() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                amount -> clientConfig.setWorldGaugeY(clientConfig.getWorldGaugeY() + amount * ConfigScreen.SHIFT_AMOUNT.get()),
                () -> { clientConfig.setWorldGaugeX(0); clientConfig.setWorldGaugeY(0); },
                false, false, true, Component.translatable("cold_sweat.config.world_temp_location.desc").getString(),
                           "§7"+Component.translatable("cold_sweat.config.offset_shift.name").getString());

        // Custom Hotbar
        this.addButton("custom_hotbar", Side.RIGHT, () -> Component.translatable("cold_sweat.config.custom_hotbar.name").getString() + ": " + (clientConfig.customHotbarEnabled() ? ON : OFF),
                button -> clientConfig.setCustomHotbar(!clientConfig.customHotbarEnabled()),
                false, false, true, Component.translatable("cold_sweat.config.custom_hotbar.desc").getString());

        // Icon Bobbing
        this.addButton("icon_bobbing", Side.RIGHT, () -> Component.translatable("cold_sweat.config.icon_bobbing.name").getString() + ": " + (clientConfig.isIconBobbingEnabled() ? ON : OFF),
                button -> clientConfig.setIconBobbing(!clientConfig.isIconBobbingEnabled()),
                false, false, true, Component.translatable("cold_sweat.config.icon_bobbing.desc").getString());

        // Config Button Repositioning Screen
        this.addButton("button_position", Side.RIGHT, () -> Component.translatable("cold_sweat.config.config_button_pos.name").getString(),
                       button ->
                       {
                           DrawConfigButton.DRAW_CONTROLS = true;
                           this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
                       },
                       false, false, true, Component.translatable("cold_sweat.config.config_button_pos.desc").getString());
    }

    @Override
    public void onClose()
    {
        super.onClose();
        ConfigScreen.saveConfig();
    }
}
