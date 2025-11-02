package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.event.RegisterItemOverrides;
import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.serialization.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class ConfigPageThree extends AbstractConfigPage
{
    public ConfigPageThree(Screen parentScreen)
    {   super(parentScreen);
    }

    @Override
    public MutableComponent sectionOneTitle()
    {   return new TranslatableComponent("cold_sweat.config.section.hud_settings");
    }

    @Nullable
    @Override
    public MutableComponent sectionTwoTitle()
    {   return new TranslatableComponent("cold_sweat.config.section.other");
    }

    @Override
    protected void init()
    {
        super.init();

        // Show Water Effect
        this.addButton("show_water_effect", Side.LEFT, () -> getEnumButtonText(new TranslatableComponent("cold_sweat.config.show_water_effect.name"), ConfigSettings.WATER_EFFECT_SETTING.get()),
                button -> ConfigSettings.WATER_EFFECT_SETTING.set(getNextCycle(ConfigSettings.WATER_EFFECT_SETTING.get())),
                false, false, true, new TranslatableComponent("cold_sweat.config.show_water_effect.desc"));

        // Water Droplet Opacity
        this.addSliderButton("water_droplet_opacity", Side.LEFT,
                             () -> getSliderPercentageText(new TranslatableComponent("cold_sweat.config.water_droplet_opacity.name"), ConfigSettings.WATER_DROPLET_OPACITY.get(), 0),
                             0, 1,
                             (value, button) -> ConfigSettings.WATER_DROPLET_OPACITY.set(value),
                             button -> button.setValue(ConfigSettings.WATER_DROPLET_OPACITY.get()),
                             false, true, new TranslatableComponent("cold_sweat.config.water_droplet_opacity.desc"));

        // Water Droplet Scale
        this.addSliderButton("water_droplet_scale", Side.LEFT,
                             () -> getSliderText(new TranslatableComponent("cold_sweat.config.water_droplet_scale.name"), ConfigSettings.WATER_DROPLET_SCALE.get().min(), 5, 100, -1),
                             5, 100,
                             (value, button) -> ConfigSettings.WATER_DROPLET_SCALE.set(new IntegerBounds(value.intValue(), (int) (value * 1.2))),
                             button -> button.setValue(CSMath.blend(0, 1, ConfigSettings.WATER_DROPLET_SCALE.get().min(), 5, 100)),
                             false, true, new TranslatableComponent("cold_sweat.config.water_droplet_scale.desc"));

        this.addEmptySpace(Side.LEFT, 0.5);

        // Animate Soulspring Lamp
        this.addButton("animate_soulspring_lamp", Side.LEFT,
                       () -> getToggleButtonText(new TranslatableComponent("cold_sweat.config.animate_soulspring_lamp.name"), ConfigSettings.ANIMATED_SOULSPRING_LAMP_MODEL.get()),
                       button ->
                       {
                           DynamicHolder<Boolean> setting = ConfigSettings.ANIMATED_SOULSPRING_LAMP_MODEL;
                           setting.set(!setting.get());
                           if (!setting.get())
                           {    ItemProperties.register(ModItems.SOULSPRING_LAMP, new ResourceLocation(ColdSweat.MOD_ID, "soulspring_state"), RegisterItemOverrides.SOULSPRING_LAMP_PROPERTIES);
                           }
                           else RegisterItemOverrides.unregister(ModItems.SOULSPRING_LAMP);
                       },
                       false, false, true, new TranslatableComponent("cold_sweat.config.animate_soulspring_lamp.desc"));

        // Enable Grace Period
        this.addButton("grace_toggle", Side.RIGHT,
                       () -> getToggleButtonText(new TranslatableComponent("cold_sweat.config.grace_period.name"), ConfigSettings.GRACE_ENABLED.get()),
                       button -> ConfigSettings.GRACE_ENABLED.set(!ConfigSettings.GRACE_ENABLED.get()),
                       true, false, false, new TranslatableComponent("cold_sweat.config.grace_period.desc"));

        // Grace Period Length
        this.addDecimalInput("grace_length", Side.RIGHT, new TranslatableComponent("cold_sweat.config.grace_period_length.name"),
                             value -> ConfigSettings.GRACE_LENGTH.set(value.intValue()),
                             input -> input.setValue(ConfigSettings.GRACE_LENGTH.get() + ""),
                             true, false, false, new TranslatableComponent("cold_sweat.config.grace_period_length.desc_1"),
                             new TranslatableComponent("cold_sweat.config.grace_period_length.desc_2").withStyle(ChatFormatting.DARK_GRAY));

        this.addEmptySpace(Side.RIGHT, 0.5);

        // Check sleep conditions
        this.addButton("check_sleep_conditions", Side.RIGHT,
                       () -> getToggleButtonText(new TranslatableComponent("cold_sweat.config.check_sleep_conditions.name"), ConfigSettings.CHECK_SLEEP_CONDITIONS.get()),
                       button -> ConfigSettings.CHECK_SLEEP_CONDITIONS.set(!ConfigSettings.CHECK_SLEEP_CONDITIONS.get()),
                       true, false, false, new TranslatableComponent("cold_sweat.config.check_sleep_conditions.desc"));

        this.addEmptySpace(Side.RIGHT, 0.5);

        // Insulation Strength
        this.addDecimalInput("insulation_strength", Side.RIGHT, new TranslatableComponent("cold_sweat.config.insulation_strength.name"),
                             value -> ConfigSettings.INSULATION_STRENGTH.set(value),
                             input -> input.setValue(ConfigSettings.INSULATION_STRENGTH.get() + ""),
                             true, false, false,
                             new TranslatableComponent("cold_sweat.config.insulation_strength.desc"));

        // Modifier Tick Rate
        this.addSliderButton("modifier_tick_rate", Side.RIGHT,
                             () -> getSliderPercentageText(new TranslatableComponent("cold_sweat.config.modifier_tick_rate.name"), ConfigSettings.MODIFIER_TICK_RATE.get(), 10),
                             0.1, 1,
                             (value, button) -> ConfigSettings.MODIFIER_TICK_RATE.set(value),
                             (button) -> button.setValue(CSMath.blend(0, 1, ConfigSettings.MODIFIER_TICK_RATE.get(), 0.1, 1)),
                             true, false,
                             new TranslatableComponent("cold_sweat.config.modifier_tick_rate.desc"));
    }

    @Override
    public void onClose()
    {   super.onClose();
        ConfigScreen.saveConfig();
    }
}
