package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigPageTempEffects extends AbstractConfigPage
{
    public ConfigPageTempEffects(Screen parentScreen)
    {   super(parentScreen);
    }

    @Override
    public ITextComponent sectionOneTitle()
    {   return new TranslationTextComponent("cold_sweat.config.section.cold");
    }

    @Override
    public ITextComponent sectionTwoTitle()
    {   return new TranslationTextComponent("cold_sweat.config.section.heat");
    }

    @Override
    public boolean showNavigation()
    {   return false;
    }

    @Override
    protected void init()
    {
        super.init();

        // Show Frozen Health
        this.addButton("show_frozen_health", Side.LEFT,
                       () -> getToggleButtonText(new TranslationTextComponent("cold_sweat.config.show_frozen_health.name"), ConfigSettings.SHOW_FROZEN_HEALTH.get()),
                       (button) -> ConfigSettings.SHOW_FROZEN_HEALTH.set(!ConfigSettings.SHOW_FROZEN_HEALTH.get()),
                       false, false, true,
                       new TranslationTextComponent("cold_sweat.config.show_frozen_health.desc"));

        // Freezing Overlay Opacity
        this.addSliderButton("freezing_overlay_opacity", Side.LEFT,
                             () -> getSliderPercentageText(new TranslationTextComponent("cold_sweat.config.freeze_overlay_opacity.name"), ConfigSettings.FREEZING_OVERLAY_OPACITY.get(), 0),
                             0, 1,
                             (value, button) -> ConfigSettings.FREEZING_OVERLAY_OPACITY.set(value),
                             (button) -> button.setValue(ConfigSettings.FREEZING_OVERLAY_OPACITY.get()),
                             true, true,
                             new TranslationTextComponent("cold_sweat.config.freeze_overlay_opacity.desc"));

        // Shiver Intensity (0 to 2.0)
        this.addSliderButton("shiver_intensity", Side.LEFT,
                             () -> getSliderPercentageText(new TranslationTextComponent("cold_sweat.config.shiver_intensity.name"), ConfigSettings.SHIVER_INTENSITY.get(), 0),
                             0, 2.0,
                             (value, button) -> ConfigSettings.SHIVER_INTENSITY.set(value),
                             (button) -> button.setValue(ConfigSettings.SHIVER_INTENSITY.get() / 2),
                             false, true,
                             new TranslationTextComponent("cold_sweat.config.shiver_intensity.desc"));

        // Freezing Hearts Percentage
        this.addSliderButton("freezing_hearts", Side.LEFT,
                             () -> getSliderPercentageText(new TranslationTextComponent("cold_sweat.config.cold_freezing_hearts.name"), ConfigSettings.HEARTS_FREEZING_PERCENTAGE.get(), 0),
                             0, 1,
                             (value, button) -> ConfigSettings.HEARTS_FREEZING_PERCENTAGE.set(value),
                             (button) -> button.setValue(ConfigSettings.HEARTS_FREEZING_PERCENTAGE.get()),
                             true, false,
                             new TranslationTextComponent("cold_sweat.config.cold_freezing_hearts.desc"));

        // Cold Mining Speed
        this.addSliderButton("cold_mining_speed", Side.LEFT,
                             () -> getSliderPercentageText(new TranslationTextComponent("cold_sweat.config.cold_mining_impairment.name"), ConfigSettings.COLD_MINING_IMPAIRMENT.get(), 0),
                             0, 1,
                             (value, button) -> ConfigSettings.COLD_MINING_IMPAIRMENT.set(value),
                             (button) -> button.setValue(ConfigSettings.COLD_MINING_IMPAIRMENT.get()),
                             true, false,
                             new TranslationTextComponent("cold_sweat.config.cold_mining_impairment.desc"));

        // Cold Movement Speed
        this.addSliderButton("cold_movement_speed", Side.LEFT,
                             () -> getSliderPercentageText(new TranslationTextComponent("cold_sweat.config.cold_movement_slowdown.name"), ConfigSettings.COLD_MOVEMENT_SLOWDOWN.get(), 0),
                             0, 1,
                             (value, button) -> ConfigSettings.COLD_MOVEMENT_SLOWDOWN.set(value),
                             (button) -> button.setValue(ConfigSettings.COLD_MOVEMENT_SLOWDOWN.get()),
                             true, false,
                             new TranslationTextComponent("cold_sweat.config.cold_movement_slowdown.desc"));

        // Cold Knockback Reduction
        this.addSliderButton("cold_knockback_reduction", Side.LEFT,
                             () -> getSliderPercentageText(new TranslationTextComponent("cold_sweat.config.cold_knockback_reduction.name"), ConfigSettings.COLD_KNOCKBACK_REDUCTION.get(), 0),
                             0, 1,
                             (value, button) -> ConfigSettings.COLD_KNOCKBACK_REDUCTION.set(value),
                             (button) -> button.setValue(ConfigSettings.COLD_KNOCKBACK_REDUCTION.get()),
                             true, false,
                             new TranslationTextComponent("cold_sweat.config.cold_knockback_reduction.desc"));

        // Heat Border Opacity
        this.addSliderButton("heat_border_opacity", Side.RIGHT,
                             () -> getSliderPercentageText(new TranslationTextComponent("cold_sweat.config.heat_border_opacity.name"), ConfigSettings.HEATSTROKE_BORDER_OPACITY.get(), 0),
                             0, 1,
                             (value, button) -> ConfigSettings.HEATSTROKE_BORDER_OPACITY.set(value),
                             (button) -> button.setValue(ConfigSettings.HEATSTROKE_BORDER_OPACITY.get()),
                             false, true,
                             new TranslationTextComponent("cold_sweat.config.heat_border_opacity.desc"));

        // Heat Blur Amount
        this.addSliderButton("heat_blur_amount", Side.RIGHT,
                             () -> getSliderPercentageText(new TranslationTextComponent("cold_sweat.config.heat_blur_amount.name"), ConfigSettings.HEATSTROKE_BLUR_AMOUNT.get(), 0),
                             0, 1,
                             (value, button) -> ConfigSettings.HEATSTROKE_BLUR_AMOUNT.set(value),
                             (button) -> button.setValue(ConfigSettings.HEATSTROKE_BLUR_AMOUNT.get()),
                             false, true,
                             new TranslationTextComponent("cold_sweat.config.heat_blur_amount.desc"));

        // Heat Sway Amount (0 to 2.0)
        this.addSliderButton("heat_sway_amount", Side.RIGHT,
                             () -> getSliderPercentageText(new TranslationTextComponent("cold_sweat.config.heat_sway_amount.name"), ConfigSettings.HEATSTROKE_SWAY_AMOUNT.get(), 0),
                             0, 2.0,
                             (value, button) -> ConfigSettings.HEATSTROKE_SWAY_AMOUNT.set(value),
                             (button) -> button.setValue(ConfigSettings.HEATSTROKE_SWAY_AMOUNT.get() / 2),
                             false, true,
                             new TranslationTextComponent("cold_sweat.config.heat_sway_amount.desc"));

        // Heat Sway Speed (0 to 2.0)
        this.addSliderButton("heat_sway_speed", Side.RIGHT,
                             () -> getSliderPercentageText(new TranslationTextComponent("cold_sweat.config.heat_sway_speed.name"), ConfigSettings.HEATSTROKE_SWAY_SPEED.get(), 0),
                             0, 2.0,
                             (value, button) -> ConfigSettings.HEATSTROKE_SWAY_SPEED.set(value),
                             (button) -> button.setValue(ConfigSettings.HEATSTROKE_SWAY_SPEED.get() / 2),
                             false, true,
                             new TranslationTextComponent("cold_sweat.config.heat_sway_speed.desc"));

        // Heat Fog Distance
        this.addSliderButton("heat_fog_distance", Side.RIGHT,
                             () -> getSliderText(new TranslationTextComponent("cold_sweat.config.heat_fog_distance.name"), ConfigSettings.HEATSTROKE_FOG_DISTANCE.get().intValue(), 0, 64, 64),
                             0, 1,
                             (value, button) -> ConfigSettings.HEATSTROKE_FOG_DISTANCE.set(value * 64),
                             (button) -> button.setValue(ConfigSettings.HEATSTROKE_FOG_DISTANCE.get() / 64),
                             true, false,
                             new TranslationTextComponent("cold_sweat.config.heat_fog_distance.desc"));
    }
}
