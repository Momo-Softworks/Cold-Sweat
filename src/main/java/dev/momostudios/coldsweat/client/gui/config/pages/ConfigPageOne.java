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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
        on = new TranslationTextComponent("options.on").getString();
        off = new TranslationTextComponent("options.off").getString();
    }

    @Override
    public int index()
    {
        return 0;
    }

    @Override
    public ITextComponent sectionOneTitle()
    {
        return new TranslationTextComponent("cold_sweat.config.section.temperature_details");
    }

    @Override
    public ITextComponent sectionTwoTitle()
    {
        return new TranslationTextComponent("cold_sweat.config.section.difficulty.name");
    }

    @Override
    protected void init()
    {
        super.init();

        ClientSettingsConfig clientConfig = ClientSettingsConfig.getInstance();

        Temperature.Units[] properUnits = {clientConfig.isCelsius() ? Temperature.Units.C : Temperature.Units.F};

        /*
         The Options
        */

        // Celsius
        this.addButton("units", Side.LEFT, () -> new TranslationTextComponent("cold_sweat.config.units.name").getString() + ": " +
                (clientConfig.isCelsius() ? new TranslationTextComponent("cold_sweat.config.celsius.name").getString() :
                new TranslationTextComponent("cold_sweat.config.fahrenheit.name").getString()), button ->
        {
            PlayerEntity player = Minecraft.getInstance().player;

            clientConfig.setCelsius(!clientConfig.isCelsius());
            // Update the world temp. gauge when the button is pressed
            if (player != null)
                Overlays.WORLD_TEMP = CSMath.convertTemp(player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).map(cap -> cap.getTemp(Temperature.Type.WORLD)).orElse(0d), Temperature.Units.MC, properUnits[0], true);

            properUnits[0] = clientConfig.isCelsius() ? Temperature.Units.C : Temperature.Units.F;

            // Change the max & min temps to reflect the new setting
            ((TextFieldWidget) this.widgetBatches.get("max_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    CSMath.convertTemp(ConfigSettings.MAX_TEMP.get(), Temperature.Units.MC, properUnits[0], true))));

            ((TextFieldWidget) this.widgetBatches.get("min_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    CSMath.convertTemp(ConfigSettings.MIN_TEMP.get(), Temperature.Units.MC, properUnits[0], true))));
        }, false, false, true, new TranslationTextComponent("cold_sweat.config.units.desc").getString());


        // Temp Offset
        this.addDecimalInput("temp_offset", Side.LEFT, new TranslationTextComponent("cold_sweat.config.temp_offset.name"),
                value -> clientConfig.setTempOffset(value.intValue()),
                input -> input.setValue(String.valueOf(clientConfig.getTempOffset())),
                false, false, true, new TranslationTextComponent("cold_sweat.config.temp_offset.desc").getString()+"§r");

        // Max Temperature
        this.addDecimalInput("max_temp", Side.LEFT, new TranslationTextComponent("cold_sweat.config.max_temperature.name"),
                value -> ConfigSettings.MAX_TEMP.set(CSMath.convertTemp(value, properUnits[0], Temperature.Units.MC, true)),
                input -> input.setValue(String.valueOf(CSMath.convertTemp(ConfigSettings.MAX_TEMP.get(), Temperature.Units.MC, properUnits[0], true))),
                true, false, false, new TranslationTextComponent("cold_sweat.config.max_temperature.desc").getString());

        // Min Temperature
        this.addDecimalInput("min_temp", Side.LEFT, new TranslationTextComponent("cold_sweat.config.min_temperature.name"),
                value -> ConfigSettings.MIN_TEMP.set(CSMath.convertTemp(value, properUnits[0], Temperature.Units.MC, true)),
                input -> input.setValue(String.valueOf(CSMath.convertTemp(ConfigSettings.MIN_TEMP.get(), Temperature.Units.MC, properUnits[0], true))),
                true, false, false, new TranslationTextComponent("cold_sweat.config.min_temperature.desc").getString());

        // Rate Multiplier
        this.addDecimalInput("rate", Side.LEFT, new TranslationTextComponent("cold_sweat.config.temperature_rate.name"),
                value -> ConfigSettings.TEMP_RATE.set(value),
                input -> input.setValue(String.valueOf(ConfigSettings.TEMP_RATE.get())),
                true, false, false, new TranslationTextComponent("cold_sweat.config.temperature_rate.desc").getString());

        // Difficulty button
        this.addButton("difficulty", Side.RIGHT, () -> new TranslationTextComponent("cold_sweat.config.difficulty.name").getString() +
                        " (" + ConfigPageDifficulty.getDifficultyName(ConfigSettings.DIFFICULTY.get()) + ")...",
                button -> mc.setScreen(new ConfigPageDifficulty(this)),
                true, false, false, new TranslationTextComponent("cold_sweat.config.difficulty.desc").getString());

        this.addEmptySpace(Side.RIGHT, 1);


        // Misc. Temp Effects
        this.addButton("ice_resistance", Side.RIGHT,
                () -> new TranslationTextComponent("cold_sweat.config.ice_resistance.name").getString() + ": " + (ConfigSettings.ICE_RESISTANCE_ENABLED.get() ? on : off),
                button -> ConfigSettings.ICE_RESISTANCE_ENABLED.set(!ConfigSettings.ICE_RESISTANCE_ENABLED.get()),
                true, true, false, new TranslationTextComponent("cold_sweat.config.ice_resistance.desc").getString());

        this.addButton("fire_resistance", Side.RIGHT,
                () -> new TranslationTextComponent("cold_sweat.config.fire_resistance.name").getString() + ": " + (ConfigSettings.FIRE_RESISTANCE_ENABLED.get() ? on : off),
                button -> ConfigSettings.FIRE_RESISTANCE_ENABLED.set(!ConfigSettings.FIRE_RESISTANCE_ENABLED.get()),
                true, true, false, new TranslationTextComponent("cold_sweat.config.fire_resistance.desc").getString());

        this.addButton("require_thermometer", Side.RIGHT,
                () -> new TranslationTextComponent("cold_sweat.config.require_thermometer.name").getString() + ": " + (ConfigSettings.REQUIRE_THERMOMETER.get() ? on : off),
                button -> ConfigSettings.REQUIRE_THERMOMETER.set(!ConfigSettings.REQUIRE_THERMOMETER.get()),
                true, true, false, new TranslationTextComponent("cold_sweat.config.require_thermometer.desc").getString());

        this.addButton("damage_scaling", Side.RIGHT,
                () -> new TranslationTextComponent("cold_sweat.config.damage_scaling.name").getString() + ": " + (ConfigSettings.DAMAGE_SCALING.get() ? on : off),
                button -> ConfigSettings.DAMAGE_SCALING.set(!ConfigSettings.DAMAGE_SCALING.get()),
                true, true, false, new TranslationTextComponent("cold_sweat.config.damage_scaling.desc").getString());
    }

    @Override
    public void onClose()
    {
        ConfigScreen.saveConfig();
        super.onClose();
    }
}
