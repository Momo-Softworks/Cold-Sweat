package dev.momostudios.coldsweat.client.gui.config.pages;

import dev.momostudios.coldsweat.client.gui.config.ConfigPageBase;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ConfigPageOne extends ConfigPageBase
{
    Screen parentScreen;
    ConfigCache configCache;
    private final String ON;
    private final String OFF;

    public ConfigPageOne(Screen parentScreen, ConfigCache configCache)
    {
        super(parentScreen, configCache);
        if (parentScreen == null)
        {
            parentScreen = Minecraft.getInstance().screen;
        }
        this.parentScreen = parentScreen;
        this.configCache = configCache;
        ON = new TranslatableComponent("options.on").getString();
        OFF = new TranslatableComponent("options.off").getString();
    }

    @Override
    public int index()
    {
        return 0;
    }

    @Override
    public BaseComponent sectionOneTitle()
    {
        return new TranslatableComponent("cold_sweat.config.section.temperature_details");
    }

    @Override
    public BaseComponent sectionTwoTitle()
    {
        return new TranslatableComponent("cold_sweat.config.section.difficulty.name");
    }

    @Override
    protected void init()
    {
        super.init();

        ClientSettingsConfig clientConfig = ClientSettingsConfig.getInstance();

        // The options

        // Celsius
        this.addButton("units", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.units.name").getString() + ": " +
                (clientConfig.celsius() ? new TranslatableComponent("cold_sweat.config.celsius.name").getString() :
                new TranslatableComponent("cold_sweat.config.fahrenheit.name").getString()), button ->
        {
            clientConfig.setCelsius(!clientConfig.celsius());

            button.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.units.name").getString() + ": " +
                    (clientConfig.celsius() ? new TranslatableComponent("cold_sweat.config.celsius.name").getString() :
                            new TranslatableComponent("cold_sweat.config.fahrenheit.name").getString())));

            ((EditBox) this.elementBatches.get("max_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    CSMath.convertUnits(configCache.maxTemp, Temperature.Units.MC, clientConfig.celsius() ? Temperature.Units.C : Temperature.Units.F, true))));

            ((EditBox) this.elementBatches.get("min_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    CSMath.convertUnits(configCache.minTemp, Temperature.Units.MC, clientConfig.celsius() ? Temperature.Units.C : Temperature.Units.F, true))));
        }, false, false);


        // Temp Offset
        this.addInput("temp_offset", Side.LEFT, new TranslatableComponent("cold_sweat.config.temp_offset.name"),
                value -> clientConfig.setTempOffset(value.intValue()),
                input -> input.setValue(String.valueOf(clientConfig.tempOffset())),
                false);

        // Max Temperature
        this.addInput("max_temp", Side.LEFT, new TranslatableComponent("cold_sweat.config.max_temperature.name"),
                value -> configCache.maxTemp = CSMath.convertUnits(value, clientConfig.celsius() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true),
                input -> input.setValue(String.valueOf(CSMath.convertUnits(configCache.maxTemp, Temperature.Units.MC, clientConfig.celsius() ? Temperature.Units.C : Temperature.Units.F, true))),
                false);

        // Min Temperature
        this.addInput("min_temp", Side.LEFT, new TranslatableComponent("cold_sweat.config.min_temperature.name"),
                value -> configCache.minTemp = CSMath.convertUnits(value, clientConfig.celsius() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true),
                input -> input.setValue(String.valueOf(CSMath.convertUnits(configCache.minTemp, Temperature.Units.MC, clientConfig.celsius() ? Temperature.Units.C : Temperature.Units.F, true))),
                false);

        // Rate Multiplier
        this.addInput("rate", Side.LEFT, new TranslatableComponent("cold_sweat.config.rate_multiplier.name"),
                value -> configCache.rate = value,
                input -> input.setValue(String.valueOf(configCache.rate)),
                false);

        // Difficulty button
        this.addButton("difficulty", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.difficulty.name").getString() +
                        " (" + ConfigScreen.difficultyName(configCache.difficulty) + ")...",
                button -> mc.setScreen(new ConfigPageDifficulty(this, configCache)), true, false);

        this.addEmptySpace(Side.RIGHT, 1);


        // Misc. Temp Effects
        this.addButton("ice_resistance", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.ice_resistance.name").getString() + ": " + (configCache.iceRes ? ON : OFF),
                button ->
                {
                    configCache.iceRes = !configCache.iceRes;
                }, true, true);

        this.addButton("fire_resistance", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.fire_resistance.name").getString() + ": " + (configCache.fireRes ? ON : OFF),
                button ->
                {
                   configCache.fireRes = !configCache.fireRes;
                }, true, true);

        this.addButton("show_ambient", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.require_thermometer.name").getString() + ": " + (configCache.showWorldTemp ? ON : OFF),
                button ->
                {
                    configCache.showWorldTemp = !configCache.showWorldTemp;
                }, true, true);

        this.addButton("damage_scaling", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.damage_scaling.name").getString() + ": " + (configCache.damageScaling ? ON : OFF),
                button ->
                {
                    configCache.damageScaling = !configCache.damageScaling;
                }, true, true);
    }

    @Override
    public void onClose()
    {
        ConfigScreen.saveConfig(configCache);
        super.onClose();
    }
}
