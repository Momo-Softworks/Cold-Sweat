package dev.momostudios.coldsweat.client.gui.config.pages;

import dev.momostudios.coldsweat.client.gui.config.ConfigButton;
import dev.momostudios.coldsweat.client.gui.config.ConfigPageBase;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.components.Button;
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

    boolean celsius = ClientSettingsConfig.getInstance().celsius();

    public EditBox tempOffsetInput;
    public EditBox maxTempInput;
    public EditBox minTempInput;
    public EditBox rateMultInput;
    public Button difficultyButton;
    public Button celsiusButton;
    public Button iceResButton;
    public Button fireResButton;
    public Button damageScalingButton;
    public Button showWorldTempButton;

    public ConfigPageOne(Screen parentScreen, ConfigCache configCache)
    {
        super(parentScreen, configCache);
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
    public BaseComponent sectionTwoTitle()
    {
        return new TranslatableComponent("cold_sweat.config.section.difficulty.name");
    }

    @Override
    protected void init()
    {
        super.init();

        // The options

        // Celsius
        celsiusButton = new ConfigButton(this.width / 2 - 185, this.height / 4 - 8, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.units.name").getString() + ": " +
                        (this.celsius ? new TranslatableComponent("cold_sweat.config.celsius.name").getString() :
                                new TranslatableComponent("cold_sweat.config.fahrenheit.name").getString())), button -> this.toggleCelsius(), configCache)
        {
            @Override
            public boolean setsCustomDifficulty()
            {
                return false;
            }
        };
        celsiusButton.setWidth(Math.max(152, font.width(celsiusButton.getMessage().getString())));


        // Temp Offset
        this.tempOffsetInput = new EditBox(font, 0, this.height / 4 + 20, 51, 22, new TextComponent(""));
        this.tempOffsetInput.setValue(String.valueOf(ClientSettingsConfig.getInstance().tempOffset()));

        // Max Temperature
        this.maxTempInput = new EditBox(font, 0, this.height / 4 + 52, 51, 22, new TextComponent(""));
        this.maxTempInput.setValue(String.valueOf(ConfigScreen.twoPlaces.format(
                CSMath.convertUnits(configCache.maxTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true))));

        // Min Temperature
        this.minTempInput = new EditBox(font, 0, this.height / 4 + 84, 51, 22, new TextComponent(""));
        this.minTempInput.setValue(String.valueOf(ConfigScreen.twoPlaces.format(
                CSMath.convertUnits(configCache.minTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true))));

        // Rate Multiplier
        this.rateMultInput = new EditBox(font, 0, this.height / 4 + 116, 51, 22, new TextComponent(""));
        this.rateMultInput.setValue(String.valueOf(configCache.rate));

        // Difficulty button
        difficultyButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.difficulty.name").getString() +
                        " (" + ConfigScreen.difficultyName(configCache.difficulty) + ")..."),
                button -> mc.setScreen(new ConfigPageDifficulty(this, configCache)), configCache)
        {
            @Override
            public boolean setsCustomDifficulty()
            {
                return false;
            }
        };
        difficultyButton.setWidth(Math.max(152, font.width(difficultyButton.getMessage().getString()) + 4));


        // Misc. Temp Effects
        iceResButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + ConfigScreen.OPTION_SIZE * 2, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.ice_resistance.name").getString() + ": " + (configCache.iceRes ? ON : OFF)),
                button -> this.toggleIceRes(), configCache);
        iceResButton.setWidth(Math.max(152, font.width(iceResButton.getMessage().getString()) + 4));

        fireResButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + ConfigScreen.OPTION_SIZE * 3, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.fire_resistance.name").getString() + ": " + (configCache.fireRes ? ON : OFF)),
                button -> this.toggleFireRes(), configCache);
        fireResButton.setWidth(Math.max(152, font.width(fireResButton.getMessage().getString()) + 4));

        showWorldTempButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + ConfigScreen.OPTION_SIZE * 4, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.require_thermometer.name").getString() + ": " + (configCache.showWorldTemp ? ON : OFF)),
                button -> this.toggleShowWorldTemp(), configCache);
        showWorldTempButton.setWidth(Math.max(152, font.width(showWorldTempButton.getMessage().getString()) + 4));

        damageScalingButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + ConfigScreen.OPTION_SIZE * 5, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.damage_scaling.name").getString() + ": " + (configCache.damageScaling ? ON : OFF)),
                button -> this.toggleDamageScaling(), configCache);
        damageScalingButton.setWidth(Math.max(152, font.width(damageScalingButton.getMessage().getString()) + 4));

        this.addWidget(celsiusButton);

        this.addWidget(difficultyButton);

        this.addWidget(iceResButton);
        this.addWidget(fireResButton);
        this.addWidget(showWorldTempButton);
        this.addWidget(damageScalingButton);

        if (mc.player != null && mc.player.getPermissionLevel() < 2)
        {
            difficultyButton.active = false;
            iceResButton.active = false;
            fireResButton.active = false;
            showWorldTempButton.active = false;
            damageScalingButton.active = false;
        } else
        {
            this.addInput(this.maxTempInput, new TranslatableComponent("cold_sweat.config.max_temperature.name"), Side.LEFT, true);
            this.addInput(this.minTempInput, new TranslatableComponent("cold_sweat.config.min_temperature.name"), Side.LEFT, true);
            this.addInput(this.rateMultInput, new TranslatableComponent("cold_sweat.config.rate_multiplier.name"), Side.LEFT, true);
        }

        this.addInput(tempOffsetInput, new TranslatableComponent("cold_sweat.config.temp_offset.name"), Side.LEFT, false);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (mc.player != null && mc.player.getPermissionLevel() < 2)
        {
            maxTempInput.setEditable(false);
            maxTempInput.setValue(String.valueOf(ConfigScreen.twoPlaces.format(CSMath.convertUnits(configCache.maxTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true))));
            minTempInput.setEditable(false);
            minTempInput.setValue(String.valueOf(ConfigScreen.twoPlaces.format(CSMath.convertUnits(configCache.minTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true))));
            rateMultInput.setEditable(false);
            rateMultInput.setValue(String.valueOf(configCache.rate));
        }
    }

    private void save()
    {
        ClientSettingsConfig.getInstance().setCelsius(this.celsius);

        CSMath.tryCatch(() ->
            ClientSettingsConfig.getInstance().setTempOffset(Integer.parseInt(tempOffsetInput.getValue()))
        );

        CSMath.tryCatch(() ->
            configCache.maxTemp = CSMath.convertUnits(Double.parseDouble(maxTempInput.getValue()), celsius ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true)
        );

        CSMath.tryCatch(() ->
            configCache.minTemp = CSMath.convertUnits(Double.parseDouble(minTempInput.getValue()), celsius ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true)
        );

        CSMath.tryCatch(() ->
        {
            double rateModifier = Double.parseDouble(rateMultInput.getValue());
            configCache.rate = rateModifier;
        });

        ConfigScreen.saveConfig(configCache);

    }

    @Override
    public void onClose()
    {
        save();
        super.onClose();
    }

    public void toggleCelsius()
    {
        this.celsius = !this.celsius;
        celsiusButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.units.name").getString() + ": " +
                (this.celsius ? new TranslatableComponent("cold_sweat.config.celsius.name").getString() :
                        new TranslatableComponent("cold_sweat.config.fahrenheit.name").getString())));

        minTempInput.setValue(String.valueOf(ConfigScreen.twoPlaces.format(CSMath.convertUnits(configCache.minTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true))));
        maxTempInput.setValue(String.valueOf(ConfigScreen.twoPlaces.format(CSMath.convertUnits(configCache.maxTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true))));
    }

    public void toggleIceRes()
    {
        configCache.iceRes = !configCache.iceRes;
        iceResButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.ice_resistance.name").getString() + ": " +
                (configCache.iceRes ? ON : OFF)));
    }

    public void toggleFireRes()
    {
        configCache.fireRes = !configCache.fireRes;
        fireResButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.fire_resistance.name").getString() + ": " +
                (configCache.fireRes ? ON : OFF)));
    }

    public void toggleDamageScaling()
    {
        configCache.damageScaling = !configCache.damageScaling;
        damageScalingButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.damage_scaling.name").getString() + ": " +
                (configCache.damageScaling ? ON : OFF)));
    }

    public void toggleShowWorldTemp()
    {
        configCache.showWorldTemp = !configCache.showWorldTemp;
        showWorldTempButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.require_thermometer.name").getString() + ": " +
                (configCache.showWorldTemp ? ON : OFF)));
    }
}
