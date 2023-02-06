package dev.momostudios.coldsweat.client.gui.config.pages;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.Overlays;
import dev.momostudios.coldsweat.client.gui.config.AbstractConfigPage;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class ConfigPageOne extends AbstractConfigPage
{
    Screen parentScreen;
    ConfigSettings configSettings;
    private final String on;
    private final String off;

    public ConfigPageOne(Screen parentScreen, ConfigSettings configSettings)
    {
        super(parentScreen, configSettings);
        if (parentScreen == null)
        {
            parentScreen = Minecraft.getInstance().screen;
        }
        this.parentScreen = parentScreen;
        this.configSettings = configSettings;
        on = new TranslatableComponent("options.on").getString();
        off = new TranslatableComponent("options.off").getString();
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
            Player player = Minecraft.getInstance().player;

            clientConfig.setCelsius(!clientConfig.celsius());
            // Update the world temp. gauge when the button is pressed
            if (player != null)
                Overlays.WORLD_TEMP = CSMath.convertUnits(player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).map(cap -> cap.getTemp(Temperature.Type.WORLD)).orElse(0d), Temperature.Units.MC, properUnits.get(), true);

            // Change the max & min temps to reflect the new setting
            ((EditBox) this.widgetBatches.get("max_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    CSMath.convertUnits(configSettings.maxTemp, Temperature.Units.MC, properUnits.get(), true))));

            ((EditBox) this.widgetBatches.get("min_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    CSMath.convertUnits(configSettings.minTemp, Temperature.Units.MC, properUnits.get(), true))));
        }, false, false, true, new TranslatableComponent("cold_sweat.config.units.desc").getString());


        // Temp Offset
        this.addDecimalInput("temp_offset", Side.LEFT, new TranslatableComponent("cold_sweat.config.temp_offset.name"),
                value -> clientConfig.setTempOffset(value.intValue()),
                input -> input.setValue(String.valueOf(clientConfig.tempOffset())),
                false, false, true, new TranslatableComponent("cold_sweat.config.temp_offset.desc_1").getString(),
                              "§7"+new TranslatableComponent("cold_sweat.config.temp_offset.desc_2").getString()+"§r");

        // Max Temperature
        this.addDecimalInput("max_temp", Side.LEFT, new TranslatableComponent("cold_sweat.config.max_temperature.name"),
                value -> configSettings.maxTemp = CSMath.convertUnits(value, properUnits.get(), Temperature.Units.MC, true),
                input -> input.setValue(String.valueOf(CSMath.convertUnits(configSettings.maxTemp, Temperature.Units.MC, properUnits.get(), true))),
                false, false, false, new TranslatableComponent("cold_sweat.config.max_temperature.desc").getString());

        // Min Temperature
        this.addDecimalInput("min_temp", Side.LEFT, new TranslatableComponent("cold_sweat.config.min_temperature.name"),
                value -> configSettings.minTemp = CSMath.convertUnits(value, properUnits.get(), Temperature.Units.MC, true),
                input -> input.setValue(String.valueOf(CSMath.convertUnits(configSettings.minTemp, Temperature.Units.MC, properUnits.get(), true))),
                false, false, false, new TranslatableComponent("cold_sweat.config.min_temperature.desc").getString());

        // Rate Multiplier
        this.addDecimalInput("rate", Side.LEFT, new TranslatableComponent("cold_sweat.config.temperature_rate.name"),
                value -> configSettings.rate = value,
                input -> input.setValue(String.valueOf(configSettings.rate)),
                false, false, false, new TranslatableComponent("cold_sweat.config.temperature_rate.desc").getString());

        // Difficulty button
        this.addButton("difficulty", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.difficulty.name").getString() +
                        " (" + ConfigScreen.difficultyName(configSettings.difficulty) + ")...",
                button -> mc.setScreen(new ConfigPageDifficulty(this, configSettings)),
                true, false, false, new TranslatableComponent("cold_sweat.config.difficulty.desc").getString());

        this.addEmptySpace(Side.RIGHT, 1);


        // Misc. Temp Effects
        this.addButton("ice_resistance", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.ice_resistance.name").getString() + ": " + (configSettings.iceRes ? on : off),
                button -> configSettings.iceRes = !configSettings.iceRes,
                true, true, false, new TranslatableComponent("cold_sweat.config.ice_resistance.desc").getString());

        this.addButton("fire_resistance", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.fire_resistance.name").getString() + ": " + (configSettings.fireRes ? on : off),
                button -> configSettings.fireRes = !configSettings.fireRes,
                true, true, false, new TranslatableComponent("cold_sweat.config.fire_resistance.desc").getString());

        this.addButton("require_thermometer", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.require_thermometer.name").getString() + ": " + (configSettings.requireThermometer ? on : off),
                button -> configSettings.requireThermometer = !configSettings.requireThermometer,
                true, true, false, new TranslatableComponent("cold_sweat.config.require_thermometer.desc").getString());

        this.addButton("damage_scaling", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.damage_scaling.name").getString() + ": " + (configSettings.damageScaling ? on : off),
                button -> configSettings.damageScaling = !configSettings.damageScaling,
                true, true, false, new TranslatableComponent("cold_sweat.config.damage_scaling.desc").getString());
    }

    @Override
    public void onClose()
    {
        ConfigScreen.saveConfig(configSettings);
        super.onClose();
    }
}
