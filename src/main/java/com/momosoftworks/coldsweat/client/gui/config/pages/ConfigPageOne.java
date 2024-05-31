package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class ConfigPageOne extends AbstractConfigPage
{
    Screen parentScreen;

    public ConfigPageOne(Screen parentScreen)
    {
        super(parentScreen);
        if (parentScreen == null)
        {   parentScreen = Minecraft.getInstance().screen;
        }
        this.parentScreen = parentScreen;
    }

    @Override
    public MutableComponent sectionOneTitle()
    {   return new TranslatableComponent("cold_sweat.config.section.temperature_details");
    }

    @Override
    public @Nullable MutableComponent sectionTwoTitle()
    {   return new TranslatableComponent("cold_sweat.config.section.difficulty.name");
    }

    @Override
    protected void init()
    {
        super.init();

        Temperature.Units[] properUnits = {ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F};

        /*
         The Options
        */

        // Celsius
        this.addButton("units", Side.LEFT, () -> new TranslatableComponent("cold_sweat.config.units.name").append(": ").append(ConfigSettings.CELSIUS.get()
                                                   ? new TranslatableComponent("cold_sweat.config.celsius.name")
                                                   : new TranslatableComponent("cold_sweat.config.fahrenheit.name")),
        button ->
        {
            Player player = Minecraft.getInstance().player;

            ConfigSettings.CELSIUS.set(!ConfigSettings.CELSIUS.get());
            // Update the world temp. gauge when the button is pressed
            if (player != null)
                Overlays.WORLD_TEMP = Temperature.convert(EntityTempManager.getTemperatureCap(player).map(cap -> cap.getTrait(Temperature.Trait.WORLD)).orElse(0d), Temperature.Units.MC, properUnits[0], true);

            properUnits[0] = ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F;

            // Change the max & min temps to reflect the new setting
            ((EditBox) this.widgetBatches.get("max_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    Temperature.convert(ConfigSettings.MAX_TEMP.get(), Temperature.Units.MC, properUnits[0], true))));

            ((EditBox) this.widgetBatches.get("min_temp").get(0)).setValue(String.valueOf(ConfigScreen.TWO_PLACES.format(
                    Temperature.convert(ConfigSettings.MIN_TEMP.get(), Temperature.Units.MC, properUnits[0], true))));
        }, false, false, true, new TranslatableComponent("cold_sweat.config.units.desc"));

        // Max Temperature
        this.addDecimalInput("max_temp", Side.LEFT, new TranslatableComponent("cold_sweat.config.max_temperature.name"),
                value -> ConfigSettings.MAX_TEMP.set(Temperature.convert(value, properUnits[0], Temperature.Units.MC, true)),
                input -> input.setValue(String.valueOf(Temperature.convert(ConfigSettings.MAX_TEMP.get(), Temperature.Units.MC, properUnits[0], true))),
                true, false, false, new TranslatableComponent("cold_sweat.config.max_temperature.desc"));

        // Min Temperature
        this.addDecimalInput("min_temp", Side.LEFT, new TranslatableComponent("cold_sweat.config.min_temperature.name"),
                value -> ConfigSettings.MIN_TEMP.set(Temperature.convert(value, properUnits[0], Temperature.Units.MC, true)),
                input -> input.setValue(String.valueOf(Temperature.convert(ConfigSettings.MIN_TEMP.get(), Temperature.Units.MC, properUnits[0], true))),
                true, false, false, new TranslatableComponent("cold_sweat.config.min_temperature.desc"));

        // Temp Damage
        this.addDecimalInput("temp_damage", Side.LEFT, new TranslatableComponent("cold_sweat.config.temp_damage.name"),
                value -> ConfigSettings.TEMP_DAMAGE.set(value),
                input -> input.setValue(String.valueOf(ConfigSettings.TEMP_DAMAGE.get())),
                true, true, false, new TranslatableComponent("cold_sweat.config.temp_damage.desc"));

        // Rate Multiplier
        this.addDecimalInput("rate", Side.LEFT, new TranslatableComponent("cold_sweat.config.temperature_rate.name"),
                value -> ConfigSettings.TEMP_RATE.set(value),
                input -> input.setValue(String.valueOf(ConfigSettings.TEMP_RATE.get())),
                true, true, false, new TranslatableComponent("cold_sweat.config.temperature_rate.desc"));

        // Difficulty button
        this.addButton("difficulty", Side.RIGHT, () -> new TranslatableComponent("cold_sweat.config.difficulty.name").append(
                        " (" + ConfigPageDifficulty.getDifficultyName(ConfigSettings.DIFFICULTY.get()).getString() + ")..."),
                button -> mc.setScreen(new ConfigPageDifficulty(this)),
                true, false, false, new TranslatableComponent("cold_sweat.config.difficulty.desc"));

        this.addEmptySpace(Side.RIGHT, 1);


        // Misc. Temp Effects
        this.addButton("ice_resistance", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.ice_resistance.name").append(": ").append(ConfigSettings.ICE_RESISTANCE_ENABLED.get() ? ON : OFF),
                button -> ConfigSettings.ICE_RESISTANCE_ENABLED.set(!ConfigSettings.ICE_RESISTANCE_ENABLED.get()),
                true, true, false, new TranslatableComponent("cold_sweat.config.ice_resistance.desc"));

        this.addButton("fire_resistance", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.fire_resistance.name").append(": ").append(ConfigSettings.FIRE_RESISTANCE_ENABLED.get() ? ON : OFF),
                button -> ConfigSettings.FIRE_RESISTANCE_ENABLED.set(!ConfigSettings.FIRE_RESISTANCE_ENABLED.get()),
                true, true, false, new TranslatableComponent("cold_sweat.config.fire_resistance.desc"));

        this.addButton("require_thermometer", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.require_thermometer.name").append(": ").append(ConfigSettings.REQUIRE_THERMOMETER.get() ? ON : OFF),
                button -> ConfigSettings.REQUIRE_THERMOMETER.set(!ConfigSettings.REQUIRE_THERMOMETER.get()),
                true, true, false, new TranslatableComponent("cold_sweat.config.require_thermometer.desc"));

        this.addButton("damage_scaling", Side.RIGHT,
                () -> new TranslatableComponent("cold_sweat.config.damage_scaling.name").append(": ").append(ConfigSettings.DAMAGE_SCALING.get() ? ON : OFF),
                button -> ConfigSettings.DAMAGE_SCALING.set(!ConfigSettings.DAMAGE_SCALING.get()),
                true, true, false, new TranslatableComponent("cold_sweat.config.damage_scaling.desc"));
    }

    @Override
    public void onClose()
    {   ConfigScreen.saveConfig();
        super.onClose();
    }
}
