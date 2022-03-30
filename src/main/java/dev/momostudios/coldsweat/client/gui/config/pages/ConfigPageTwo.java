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
        this.addButton("grace_toggle", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.grace_period.name").getString() + ": " + (configCache.graceEnabled ? ON : OFF),
                button ->
                {
                    configCache.graceEnabled = !configCache.graceEnabled;

                    button.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.grace_period.name").getString() + ": "
                            + (configCache.graceEnabled ? ON : OFF)));
                },
                true, true, "Gives the player a brief time of immunity to temperature when spawning in the world.");

        // Grace Period Length
        this.addDecimalInput("grace_length", Side.LEFT, new TranslatableComponent("cold_sweat.config.grace_period_length.name"),
                value -> configCache.graceLength = value.intValue(),
                input -> input.setValue(configCache.graceLength + ""),
                true, true, "The duration of the Grace Period, in ticks.", "§71 tick = 1/20th of a second§r");

        // Direction Buttons: Steve Head
        this.addDirectionPanel("icon_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temperature_icon.name"),
                amount -> clientConfig.setTempIconX(clientConfig.tempIconX() + amount),
                amount -> clientConfig.setTempIconY(clientConfig.tempIconY() + amount),
                () -> { clientConfig.setTempIconX(0); clientConfig.setTempIconY(0); },
                false, false, "The position of the body temperature icon on the screen.");

        // Direction Buttons: Temp Readout
        this.addDirectionPanel("readout_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temperature_readout.name"),
                amount -> clientConfig.setTempReadoutX(clientConfig.tempReadoutX() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                amount -> clientConfig.setTempReadoutY(clientConfig.tempReadoutY() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                () -> { clientConfig.setTempReadoutX(0); clientConfig.setTempReadoutY(0); },
                false, false, "The position of the body temperature readout (number) on the screen.");

        this.addDirectionPanel("gauge_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temperature_gauge.name"),
                amount -> clientConfig.setTempGaugeX(clientConfig.tempGaugeX() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                amount -> clientConfig.setTempGaugeY(clientConfig.tempGaugeY() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                () -> { clientConfig.setTempGaugeX(0); clientConfig.setTempGaugeY(0); },
                false, false, "The position of the world temperature gauge on the screen.");

        // Custom Hotbar
        this.addButton("custom_hotbar", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " + (clientConfig.customHotbar() ? ON : OFF),
                button ->
                {
                    clientConfig.setCustomHotbar(!clientConfig.customHotbar());

                    button.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " + (clientConfig.customHotbar() ? ON : OFF)));
                },
                false, false, "Whether or not to use Cold Sweat's custom hotbar layout (disable this if it is clashing with other mods).");

        // Icon Bobbing
        this.addButton("icon_bobbing", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.icon_bobbing.name").getString() + ": " + (clientConfig.iconBobbing() ? ON : OFF),
                button ->
                {
                    clientConfig.setIconBobbing(!clientConfig.iconBobbing());

                    button.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.icon_bobbing.name").getString() + ": " + (clientConfig.iconBobbing() ? ON : OFF)));
                },
                false, false, "Whether or not elements of Cold Sweat's UI should \"jiggle\" in extreme temperatures.");

        this.addLabel("shift_label", Side.RIGHT, new TranslatableComponent("cold_sweat.config.offset_shift.name").getString(), 11908533);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        ConfigScreen.saveConfig(configCache);
    }
}
