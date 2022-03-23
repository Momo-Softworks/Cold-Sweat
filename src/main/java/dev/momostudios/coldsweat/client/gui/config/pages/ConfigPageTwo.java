package dev.momostudios.coldsweat.client.gui.config.pages;

import dev.momostudios.coldsweat.client.gui.config.ConfigPageBase;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class ConfigPageTwo extends ConfigPageBase
{
    private final ConfigCache configCache;
    private final String ON;
    private final String OFF;

    public ConfigPageTwo(Screen parentScreen, ConfigCache configCache)
    {
        super(parentScreen, configCache);
        this.configCache = configCache;
        ON = new TranslatableComponent("options.on").getString();
        OFF = new TranslatableComponent("options.off").getString();
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

        // Enable Grace Period
        this.addButton("grace_toggle", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.grace_period.name").getString() + ": " + (configCache.gracePeriodEnabled ? ON : OFF),
                button ->
                {
                    configCache.gracePeriodEnabled = !configCache.gracePeriodEnabled;

                    button.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.grace_period.name").getString() + ": "
                            + (configCache.gracePeriodEnabled ? ON : OFF)));
                },
                true, true);

        // Grace Period Length
        this.addInput("grace_length", Side.LEFT, new TranslatableComponent("cold_sweat.config.grace_period_length.name"),
                value -> configCache.gracePeriodLength = value.intValue(),
                input -> input.setValue(configCache.gracePeriodLength + ""),
                true);

        // Direction Buttons: Steve Head
        this.addDirectionPanel("icon_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temperature_icon.name"),
                amount -> clientConfig.setSteveHeadX(clientConfig.steveHeadX() + amount),
                amount -> clientConfig.setSteveHeadY(clientConfig.steveHeadY() + amount),
                () -> { clientConfig.setSteveHeadX(0); clientConfig.setSteveHeadY(0); },
                false);

        // Direction Buttons: Temp Readout
        this.addDirectionPanel("readout_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temperature_readout.name"),
                amount -> clientConfig.setTempGaugeX(clientConfig.tempGaugeX() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                amount -> clientConfig.setTempGaugeY(clientConfig.tempGaugeY() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                () -> { clientConfig.setTempGaugeX(0); clientConfig.setTempGaugeY(0); },
                false);

        // Custom Hotbar
        this.addButton("custom_hotbar", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " + (clientConfig.customHotbar() ? ON : OFF),
                button ->
                {
                    clientConfig.setCustomHotbar(!clientConfig.customHotbar());

                    button.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " + (clientConfig.customHotbar() ? ON : OFF)));
                },
                false, false);

        // Icon Bobbing
        this.addButton("icon_bobbing", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.icon_bobbing.name").getString() + ": " + (clientConfig.iconBobbing() ? ON : OFF),
                button ->
                {
                    clientConfig.setIconBobbing(!clientConfig.iconBobbing());

                    button.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.icon_bobbing.name").getString() + ": " + (clientConfig.iconBobbing() ? ON : OFF)));
                },
                false, false);

        this.addLabel("shift_label", Side.RIGHT, new TranslatableComponent("cold_sweat.config.offset_shift.name").getString(), 11908533);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        ConfigScreen.saveConfig(configCache);
    }
}
