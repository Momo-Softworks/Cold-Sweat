package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class ConfigPageThree extends AbstractConfigPage
{
    public ConfigPageThree(Screen parentScreen)
    {   super(parentScreen);
    }

    @Override
    public Component sectionOneTitle()
    {   return Component.translatable("cold_sweat.config.section.other");
    }

    @Nullable
    @Override
    public Component sectionTwoTitle()
    {   return Component.translatable("cold_sweat.config.section.thermal_source");
    }

    @Override
    protected void init()
    {
        super.init();

        // Enable Grace Period
        this.addButton("grace_toggle", Side.LEFT,
                       () -> getToggleButtonText(Component.translatable("cold_sweat.config.grace_period.name"), ConfigSettings.GRACE_ENABLED.get()),
                       button -> ConfigSettings.GRACE_ENABLED.set(!ConfigSettings.GRACE_ENABLED.get()),
                       true, false, false, Component.translatable("cold_sweat.config.grace_period.desc"));

        // Grace Period Length
        this.addDecimalInput("grace_length", Side.LEFT, Component.translatable("cold_sweat.config.grace_period_length.name"),
                             value -> ConfigSettings.GRACE_LENGTH.set(value.intValue()),
                             input -> input.setValue(ConfigSettings.GRACE_LENGTH.get() + ""),
                             true, false, false, Component.translatable("cold_sweat.config.grace_period_length.desc_1"),
                             Component.translatable("cold_sweat.config.grace_period_length.desc_2").withStyle(ChatFormatting.DARK_GRAY));

        this.addEmptySpace(Side.LEFT, 1);

        // Check sleep conditions
        this.addButton("check_sleep_conditions", Side.LEFT,
                       () -> getToggleButtonText(Component.translatable("cold_sweat.config.check_sleep_conditions.name"), ConfigSettings.CHECK_SLEEP_CONDITIONS.get()),
                       button -> ConfigSettings.CHECK_SLEEP_CONDITIONS.set(!ConfigSettings.CHECK_SLEEP_CONDITIONS.get()),
                       true, false, false, Component.translatable("cold_sweat.config.check_sleep_conditions.desc"));

        // Insulation Strength
        this.addDecimalInput("insulation_strength", Side.LEFT, Component.translatable("cold_sweat.config.insulation_strength.name"),
                             value -> ConfigSettings.INSULATION_STRENGTH.set(value),
                             input -> input.setValue(ConfigSettings.INSULATION_STRENGTH.get() + ""),
                             true, false, false,
                             Component.translatable("cold_sweat.config.insulation_strength.desc"));

        // Modifier Tick Rate
        this.addSliderButton("modifier_tick_rate", Side.LEFT,
                             () -> getSliderPercentageText(Component.translatable("cold_sweat.config.modifier_tick_rate.name"), ConfigSettings.MODIFIER_TICK_RATE.get(), 10),
                             0.1, 1,
                             (value, button) -> ConfigSettings.MODIFIER_TICK_RATE.set(value),
                             (button) -> button.setValue(CSMath.blend(0, 1, ConfigSettings.MODIFIER_TICK_RATE.get(), 0.1, 1)),
                             true, false,
                             Component.translatable("cold_sweat.config.modifier_tick_rate.desc"));

        // Smart Hearth
        this.addButton("smart_hearth", Side.RIGHT, () -> getToggleButtonText(Component.translatable("cold_sweat.config.smart_source.name", Component.translatable("block.cold_sweat.hearth_bottom").getString()),
                                                                            ConfigSettings.SMART_HEARTH.get()),
                       button -> ConfigSettings.SMART_HEARTH.set(!ConfigSettings.SMART_HEARTH.get()),
                       true, false, false, Component.translatable("cold_sweat.config.smart_source.desc"));

        // Smart Boiler
        this.addButton("smart_boiler", Side.RIGHT, () -> getToggleButtonText(Component.translatable("cold_sweat.config.smart_source.name", Component.translatable("block.cold_sweat.boiler").getString()),
                                                                            ConfigSettings.SMART_BOILER.get()),
                       button -> ConfigSettings.SMART_BOILER.set(!ConfigSettings.SMART_BOILER.get()),
                       true, false, false, Component.translatable("cold_sweat.config.smart_source.desc"));

        // Smart Icebox
        this.addButton("smart_icebox", Side.RIGHT, () -> getToggleButtonText(Component.translatable("cold_sweat.config.smart_source.name", Component.translatable("block.cold_sweat.icebox").getString()),
                                                                            ConfigSettings.SMART_ICEBOX.get()),
                       button -> ConfigSettings.SMART_ICEBOX.set(!ConfigSettings.SMART_ICEBOX.get()),
                       true, false, false, Component.translatable("cold_sweat.config.smart_source.desc"));

        // Source Debug
        this.addButton("source_debug", Side.RIGHT, () -> getToggleButtonText(Component.translatable("cold_sweat.config.source_debug.name"), ConfigSettings.HEARTH_DEBUG.get()),
                       button -> ConfigSettings.HEARTH_DEBUG.set(!ConfigSettings.HEARTH_DEBUG.get()),
                       true, false, false, Component.translatable("cold_sweat.config.source_debug.desc"));
    }

    @Override
    public void onClose()
    {   super.onClose();
        ConfigScreen.saveConfig();
    }
}
