package dev.momostudios.coldsweat.client.gui.config.pages;

import dev.momostudios.coldsweat.client.gui.Overlays;
import dev.momostudios.coldsweat.client.gui.config.AbstractConfigPage;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.Supplier;

public class ConfigPageOne extends AbstractConfigPage
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

        Supplier<Temperature.Units> properUnits = () -> clientConfig.celsius() ? Temperature.Units.C : Temperature.Units.F;

        // The options

        // Celsius
        this.addButton("units", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.units.name").getString() + ": " +
                (clientConfig.celsius() ? new TranslatableComponent("cold_sweat.config.celsius.name").getString() :
                new TranslatableComponent("cold_sweat.config.fahrenheit.name").getString()), button ->
        {
            clientConfig.setCelsius(!clientConfig.celsius());
            // Update the world temp. gauge when the button is pressed
            Overlays.WORLD_TEMP = CSMath.convertUnits(Overlays.PLAYER_CAP.getTemp(Temperature.Type.WORLD), Temperature.Units.MC, properUnits.get(), true);

            // Update the button text to reflect the current setting
            button.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.units.name").getString() + ": " +
                    (clientConfig.celsius() ? new TranslatableComponent("cold_sweat.config.celsius.name").getString() :
                            new TranslatableComponent("cold_sweat.config.fahrenheit.name").getString())));

            // Change the max & min temps to reflect the new setting
            ((EditBox) this.elementBatches.get("max_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    CSMath.convertUnits(configCache.maxTemp, Temperature.Units.MC, properUnits.get(), true))));

            ((EditBox) this.elementBatches.get("min_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    CSMath.convertUnits(configCache.minTemp, Temperature.Units.MC, properUnits.get(), true))));
        }, false, false, new TranslatableComponent("cold_sweat.config.units.desc").getString());


        // Temp Offset
        this.addDecimalInput("temp_offset", Side.LEFT, new TranslatableComponent("cold_sweat.config.temp_offset.name"),
                value -> clientConfig.setTempOffset(value.intValue()),
                input -> input.setValue(String.valueOf(clientConfig.tempOffset())),
                false, false, new TranslatableComponent("cold_sweat.config.temp_offset.desc_1").getString(),
                              "§7"+new TranslatableComponent("cold_sweat.config.temp_offset.desc_2").getString()+"§r");

        // Max Temperature
        this.addDecimalInput("max_temp", Side.LEFT, new TranslatableComponent("cold_sweat.config.max_temperature.name"),
                value -> configCache.maxTemp = CSMath.convertUnits(value, properUnits.get(), Temperature.Units.MC, true),
                input -> input.setValue(String.valueOf(CSMath.convertUnits(configCache.maxTemp, Temperature.Units.MC, properUnits.get(), true))),
                false, false, new TranslatableComponent("cold_sweat.config.max_temperature.desc").getString());

        // Min Temperature
        this.addDecimalInput("min_temp", Side.LEFT, new TranslatableComponent("cold_sweat.config.min_temperature.name"),
                value -> configCache.minTemp = CSMath.convertUnits(value, properUnits.get(), Temperature.Units.MC, true),
                input -> input.setValue(String.valueOf(CSMath.convertUnits(configCache.minTemp, Temperature.Units.MC, properUnits.get(), true))),
                false, false, new TranslatableComponent("cold_sweat.config.min_temperature.desc").getString());

        // Rate Multiplier
        this.addDecimalInput("rate", Side.LEFT, new TranslatableComponent("cold_sweat.config.temperature_rate.name"),
                value -> configCache.rate = value,
                input -> input.setValue(String.valueOf(configCache.rate)),
                false, false, new TranslatableComponent("cold_sweat.config.temperature_rate.desc").getString());

        // Difficulty button
        this.addButton("difficulty", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.difficulty.name").getString() +
                        " (" + ConfigScreen.difficultyName(configCache.difficulty) + ")...",
                button -> mc.setScreen(new ConfigPageDifficulty(this, configCache)),
                true, false, new TranslatableComponent("cold_sweat.config.difficulty.desc").getString());

        this.addEmptySpace(Side.RIGHT, 1);


        // Misc. Temp Effects
        this.addButton("ice_resistance", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.ice_resistance.name").getString() + ": " + (configCache.iceRes ? ON : OFF),
                button -> configCache.iceRes = !configCache.iceRes,
                true, true, new TranslatableComponent("cold_sweat.config.ice_resistance.desc").getString());

        this.addButton("fire_resistance", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.fire_resistance.name").getString() + ": " + (configCache.fireRes ? ON : OFF),
                button -> configCache.fireRes = !configCache.fireRes,
                true, true, new TranslatableComponent("cold_sweat.config.fire_resistance.desc").getString());

        this.addButton("show_world_temp", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.require_thermometer.name").getString() + ": " + (configCache.requireThermometer ? ON : OFF),
                button -> configCache.requireThermometer = !configCache.requireThermometer,
                true, true, new TranslatableComponent("cold_sweat.config.require_thermometer.desc").getString());

        this.addButton("damage_scaling", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.damage_scaling.name").getString() + ": " + (configCache.damageScaling ? ON : OFF),
                button -> configCache.damageScaling = !configCache.damageScaling,
                true, true, new TranslatableComponent("cold_sweat.config.damage_scaling.desc").getString());
    }

    @Override
    public void onClose()
    {
        ConfigScreen.saveConfig(configCache);
        super.onClose();
    }
}
