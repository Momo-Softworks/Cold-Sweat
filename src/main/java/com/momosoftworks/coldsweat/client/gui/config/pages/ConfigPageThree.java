package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import javax.xml.soap.Text;

public class ConfigPageThree extends AbstractConfigPage
{
    public ConfigPageThree(Screen parentScreen)
    {   super(parentScreen);
    }

    @Override
    public ITextComponent sectionOneTitle()
    {   return new TranslationTextComponent("cold_sweat.config.section.other");
    }

    @Nullable
    @Override
    public ITextComponent sectionTwoTitle()
    {   return new TranslationTextComponent("cold_sweat.config.section.difficulty");
    }

    @Override
    protected void init()
    {
        super.init();

        // Enable Grace Period
        this.addButton("grace_toggle", Side.LEFT, () -> new TranslationTextComponent("cold_sweat.config.grace_period.name").append(": ").append(ConfigSettings.GRACE_ENABLED.get() ? ON : OFF),
                       button ->
                       {
                           ConfigSettings.GRACE_ENABLED.set(!ConfigSettings.GRACE_ENABLED.get());
                           button.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.grace_period.name").getString() + ": "
                                                            + (ConfigSettings.GRACE_ENABLED.get() ? ON : OFF)));
                       },
                       true, false, false, new TranslationTextComponent("cold_sweat.config.grace_period.desc"));

        // Grace Period Length
        this.addDecimalInput("grace_length", Side.LEFT, new TranslationTextComponent("cold_sweat.config.grace_period_length.name"),
                             value -> ConfigSettings.GRACE_LENGTH.set(value.intValue()),
                             input -> input.setValue(ConfigSettings.GRACE_LENGTH.get() + ""),
                             true, false, false, new TranslationTextComponent("cold_sweat.config.grace_period_length.desc_1"),
                             new TranslationTextComponent("cold_sweat.config.grace_period_length.desc_2").withStyle(TextFormatting.DARK_GRAY));

        // Freezing Hearts Percentage
        this.addSliderButton("freezing_hearts", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.cold_freezing_hearts.name"),
                             0, 1,
                             (value, button) ->
                             {
                                 button.setMessagePercentage(new TranslationTextComponent("cold_sweat.config.cold_freezing_hearts.name"), value, true);
                                 ConfigSettings.HEARTS_FREEZING_PERCENTAGE.set(value);
                             },
                             (button) ->
                             {
                                 button.setMessagePercentage(new TranslationTextComponent("cold_sweat.config.cold_freezing_hearts.name"), ConfigSettings.HEARTS_FREEZING_PERCENTAGE.get(), true);
                                 button.setValue(ConfigSettings.HEARTS_FREEZING_PERCENTAGE.get());
                             },
                             true, false,
                             new TranslationTextComponent("cold_sweat.config.cold_freezing_hearts.desc"));

        // Cold Mining Speed
        this.addSliderButton("cold_mining_speed", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.cold_mining_impairment.name"),
                             0, 1,
                             (value, button) ->
                             {
                                 button.setMessagePercentage(new TranslationTextComponent("cold_sweat.config.cold_mining_impairment.name"), ConfigSettings.COLD_MINING_IMPAIRMENT.get(), true);
                                 ConfigSettings.COLD_MINING_IMPAIRMENT.set(value);
                             },
                             (button) ->
                             {
                                 button.setMessagePercentage(new TranslationTextComponent("cold_sweat.config.cold_mining_impairment.name"), ConfigSettings.COLD_MINING_IMPAIRMENT.get(), true);
                                 button.setValue(ConfigSettings.COLD_MINING_IMPAIRMENT.get());
                             },
                             true, false,
                             new TranslationTextComponent("cold_sweat.config.cold_mining_impairment.desc"));

        // Cold Movement Speed
        this.addSliderButton("cold_movement_speed", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.cold_movement_slowdown.name"),
                             0, 1,
                             (value, button) ->
                             {
                                 button.setMessagePercentage(new TranslationTextComponent("cold_sweat.config.cold_movement_slowdown.name"), ConfigSettings.COLD_MOVEMENT_SLOWDOWN.get(), true);
                                 ConfigSettings.COLD_MOVEMENT_SLOWDOWN.set(value);
                             },
                             (button) ->
                             {
                                 button.setMessagePercentage(new TranslationTextComponent("cold_sweat.config.cold_movement_slowdown.name"), ConfigSettings.COLD_MOVEMENT_SLOWDOWN.get(), true);
                                 button.setValue(ConfigSettings.COLD_MOVEMENT_SLOWDOWN.get());
                             },
                             true, false,
                             new TranslationTextComponent("cold_sweat.config.cold_movement_slowdown.desc"));

        // Cold Knockback Reduction
        this.addSliderButton("cold_knockback_reduction", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.cold_knockback_reduction.name"),
                             0, 1,
                             (value, button) ->
                             {
                                 button.setMessagePercentage(new TranslationTextComponent("cold_sweat.config.cold_knockback_reduction.name"), ConfigSettings.COLD_KNOCKBACK_REDUCTION.get(), true);
                                 ConfigSettings.COLD_KNOCKBACK_REDUCTION.set(value);
                             },
                             (button) ->
                             {
                                 button.setMessagePercentage(new TranslationTextComponent("cold_sweat.config.cold_knockback_reduction.name"), ConfigSettings.COLD_KNOCKBACK_REDUCTION.get(), true);
                                 button.setValue(ConfigSettings.COLD_KNOCKBACK_REDUCTION.get());
                             },
                             true, false,
                             new TranslationTextComponent("cold_sweat.config.cold_knockback_reduction.desc"));

        // Heat Fog Distance
        this.addSliderButton("heat_fog_distance", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.heat_fog_distance.name"),
                             0, 1,
                             (value, button) ->
                             {
                                 button.setMessage(new TranslationTextComponent("cold_sweat.config.heat_fog_distance.name")
                                                           .append(": ")
                                                           .append(value < 1 ? new StringTextComponent((int) (value * 64) + "")
                                                                             : new StringTextComponent(OFF)));
                                 ConfigSettings.HEATSTROKE_FOG_DISTANCE.set(value * 64);
                             },
                             (button) ->
                             {
                                 double value = ConfigSettings.HEATSTROKE_FOG_DISTANCE.get();
                                 button.setMessage(new TranslationTextComponent("cold_sweat.config.heat_fog_distance.name")
                                                           .append(": ")
                                                           .append(value / 64 < 1 ? new StringTextComponent((int) value + "")
                                                                             : new StringTextComponent(OFF)));
                                 button.setValue(value / 64);
                             },
                             true, false,
                             new TranslationTextComponent("cold_sweat.config.heat_fog_distance.desc"));
    }

    @Override
    public void onClose()
    {   super.onClose();
        ConfigScreen.saveConfig();
    }
}
