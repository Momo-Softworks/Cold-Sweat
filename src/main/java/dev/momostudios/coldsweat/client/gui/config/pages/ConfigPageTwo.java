package dev.momostudios.coldsweat.client.gui.config.pages;

import dev.momostudios.coldsweat.client.gui.config.AbstractConfigPage;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class ConfigPageTwo extends AbstractConfigPage
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
                true, true, new TranslatableComponent("cold_sweat.config.grace_period.desc").getString());

        // Grace Period Length
        this.addDecimalInput("grace_length", Side.LEFT, new TranslatableComponent("cold_sweat.config.grace_period_length.name"),
                value -> configCache.graceLength = value.intValue(),
                input -> input.setValue(configCache.graceLength + ""),
                true, true, new TranslatableComponent("cold_sweat.config.grace_period_length.desc_1").getString(),
                            "§7"+new TranslatableComponent("cold_sweat.config.grace_period_length.desc_2").getString()+"§r");

        // Direction Buttons: Steve Head
        this.addDirectionPanel("icon_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temp_icon_location.name"),
                amount -> clientConfig.setBodyIconX(clientConfig.bodyIconX() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                amount -> clientConfig.setBodyIconY(clientConfig.bodyIconY() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                () -> { clientConfig.setBodyIconX(0); clientConfig.setBodyIconY(0); },
                false, false, new TranslatableComponent("cold_sweat.config.temp_icon_location.desc").getString());

        // Direction Buttons: Temp Readout
        this.addDirectionPanel("readout_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.temp_readout_location.name"),
                amount -> clientConfig.setBodyReadoutX(clientConfig.bodyReadoutX() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                amount -> clientConfig.setBodyReadoutY(clientConfig.bodyReadoutY() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                () -> { clientConfig.setBodyReadoutX(0); clientConfig.setBodyReadoutY(0); },
                false, false, new TranslatableComponent("cold_sweat.config.temp_readout_location.desc").getString());

        this.addDirectionPanel("gauge_directions", Side.RIGHT, new TranslatableComponent("cold_sweat.config.world_temp_location.name"),
                amount -> clientConfig.setWorldGaugeX(clientConfig.worldGaugeX() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                amount -> clientConfig.setWorldGaugeY(clientConfig.worldGaugeY() + amount * (Screen.hasShiftDown() ? 10 : 1)),
                () -> { clientConfig.setWorldGaugeX(0); clientConfig.setWorldGaugeY(0); },
                false, false, new TranslatableComponent("cold_sweat.config.world_temp_location.desc").getString());

        // Custom Hotbar
        this.addButton("custom_hotbar", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " + (clientConfig.customHotbar() ? ON : OFF),
                button -> clientConfig.setCustomHotbar(!clientConfig.customHotbar()),
                false, false, new TranslatableComponent("cold_sweat.config.custom_hotbar.desc").getString());

        // Icon Bobbing
        this.addButton("icon_bobbing", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.icon_bobbing.name").getString() + ": " + (clientConfig.iconBobbing() ? ON : OFF),
                button -> clientConfig.setIconBobbing(!clientConfig.iconBobbing()),
                false, false, new TranslatableComponent("cold_sweat.config.icon_bobbing.desc").getString());

        this.addLabel("shift_label", Side.RIGHT, new TranslatableComponent("cold_sweat.config.offset_shift.name").getString(), 11908533);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        ConfigScreen.saveConfig(configCache);
    }
}
