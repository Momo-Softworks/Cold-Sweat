package dev.momostudios.coldsweat.client.gui.config.pages;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.Overlays;
import dev.momostudios.coldsweat.client.gui.config.AbstractConfigPage;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ConfigSettings;
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
    private final String on;
    private final String off;

    public ConfigPageOne(Screen parentScreen)
    {
        super(parentScreen);
        if (parentScreen == null)
        {
            parentScreen = Minecraft.getInstance().screen;
        }
        this.parentScreen = parentScreen;
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
                Overlays.WORLD_TEMP = CSMath.convertTemp(player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).map(cap -> cap.getTemp(Temperature.Type.WORLD)).orElse(0d), Temperature.Units.MC, properUnits.get(), true);

            // Change the max & min temps to reflect the new setting
            ((EditBox) this.widgetBatches.get("max_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    CSMath.convertTemp(ConfigSettings.MAX_TEMP.get(), Temperature.Units.MC, properUnits.get(), true))));

            ((EditBox) this.widgetBatches.get("min_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    CSMath.convertTemp(ConfigSettings.MIN_TEMP.get(), Temperature.Units.MC, properUnits.get(), true))));
        }, false, false, true, new TranslatableComponent("cold_sweat.config.units.desc").getString());


        // Temp Offset
        this.addDecimalInput("temp_offset", Side.LEFT, new TranslatableComponent("cold_sweat.config.temp_offset.name"),
                value -> clientConfig.setTempOffset(value.intValue()),
                input -> input.setValue(String.valueOf(clientConfig.tempOffset())),
                false, false, true, new TranslatableComponent("cold_sweat.config.temp_offset.desc").getString()+"§r");

        // Max Temperature
        this.addDecimalInput("max_temp", Side.LEFT, new TranslatableComponent("cold_sweat.config.max_temperature.name"),
                value -> ConfigSettings.MAX_TEMP.set(CSMath.convertTemp(value, properUnits.get(), Temperature.Units.MC, true)),
                input -> input.setValue(String.valueOf(CSMath.convertTemp(ConfigSettings.MAX_TEMP.get(), Temperature.Units.MC, properUnits.get(), true))),
                true, false, false, new TranslatableComponent("cold_sweat.config.max_temperature.desc").getString());

        // Min Temperature
        this.addDecimalInput("min_temp", Side.LEFT, new TranslatableComponent("cold_sweat.config.min_temperature.name"),
                value -> ConfigSettings.MIN_TEMP.set(CSMath.convertTemp(value, properUnits.get(), Temperature.Units.MC, true)),
                input -> input.setValue(String.valueOf(CSMath.convertTemp(ConfigSettings.MIN_TEMP.get(), Temperature.Units.MC, properUnits.get(), true))),
                true, false, false, new TranslatableComponent("cold_sweat.config.min_temperature.desc").getString());

        // Rate Multiplier
        this.addDecimalInput("rate", Side.LEFT, new TranslatableComponent("cold_sweat.config.temperature_rate.name"),
                value -> ConfigSettings.TEMP_RATE.set(value),
                input -> input.setValue(String.valueOf(ConfigSettings.TEMP_RATE.get())),
                true, false, false, new TranslatableComponent("cold_sweat.config.temperature_rate.desc").getString());

        // Difficulty button
        this.addButton("difficulty", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.difficulty.name").getString() +
                        " (" + ConfigPageDifficulty.difficultyName(ConfigSettings.DIFFICULTY.get()) + ")...",
                button -> mc.setScreen(new ConfigPageDifficulty(this)),
                true, false, false, new TranslatableComponent("cold_sweat.config.difficulty.desc").getString());

        this.addEmptySpace(Side.RIGHT, 1);


        // Misc. Temp Effects
        this.addButton("ice_resistance", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.ice_resistance.name").getString() + ": " + (ConfigSettings.ICE_RESISTANCE_ENABLED.get() ? on : off),
                button -> ConfigSettings.ICE_RESISTANCE_ENABLED.set(!ConfigSettings.ICE_RESISTANCE_ENABLED.get()),
                true, true, false, new TranslatableComponent("cold_sweat.config.ice_resistance.desc").getString());

        this.addButton("fire_resistance", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.fire_resistance.name").getString() + ": " + (ConfigSettings.FIRE_RESISTANCE_ENABLED.get() ? on : off),
                button -> ConfigSettings.FIRE_RESISTANCE_ENABLED.set(!ConfigSettings.FIRE_RESISTANCE_ENABLED.get()),
                true, true, false, new TranslatableComponent("cold_sweat.config.fire_resistance.desc").getString());

        this.addButton("require_thermometer", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.require_thermometer.name").getString() + ": " + (ConfigSettings.REQUIRE_THERMOMETER.get() ? on : off),
                button -> ConfigSettings.REQUIRE_THERMOMETER.set(!ConfigSettings.REQUIRE_THERMOMETER.get()),
                true, true, false, new TranslatableComponent("cold_sweat.config.require_thermometer.desc").getString());

        this.addButton("damage_scaling", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.damage_scaling.name").getString() + ": " + (ConfigSettings.DAMAGE_SCALING.get() ? on : off),
                button -> ConfigSettings.DAMAGE_SCALING.set(!ConfigSettings.DAMAGE_SCALING.get()),
                true, true, false, new TranslatableComponent("cold_sweat.config.damage_scaling.desc").getString());
    }

    @Override
    public void onClose()
    {
        ConfigScreen.saveConfig();
        super.onClose();
    }
}
