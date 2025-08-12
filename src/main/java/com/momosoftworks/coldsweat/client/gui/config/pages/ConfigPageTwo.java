package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.event.DrawConfigButton;
import com.momosoftworks.coldsweat.client.event.RegisterItemOverrides;
import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Vec2i;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.serialization.DynamicHolder;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class ConfigPageTwo extends AbstractConfigPage
{
    public ConfigPageTwo(Screen parentScreen)
    {   super(parentScreen);
    }

    @Override
    public ITextComponent sectionOneTitle()
    {
        return new TranslationTextComponent("cold_sweat.config.section.preference");
    }

    @Nullable
    @Override
    public ITextComponent sectionTwoTitle()
    {
        return new TranslationTextComponent("cold_sweat.config.section.hud_settings");
    }

    @Override
    protected void init()
    {
        super.init();

        // Temp Offset
        this.addDecimalInput("temp_offset", Side.LEFT, new TranslationTextComponent("cold_sweat.config.temp_offset.name"),
                             value -> ConfigSettings.TEMP_OFFSET.set(value.intValue()),
                             input -> input.setValue(String.valueOf(ConfigSettings.TEMP_OFFSET.get())),
                             false, false, true, new TranslationTextComponent("cold_sweat.config.temp_offset.desc"));

        // Distortion Effects
        this.addButton("distortion_effects", Side.LEFT, () -> getToggleButtonText(new TranslationTextComponent("cold_sweat.config.distortion.name"), ConfigSettings.DISTORTION_EFFECTS.get()),
                button ->
                {
                    ConfigSettings.HEARTH_DEBUG.set(!ConfigSettings.HEARTH_DEBUG.get());
                },
                false, false, true, new TranslationTextComponent("cold_sweat.config.hearth_debug.desc"));

        // High Contrast
        this.addButton("high_contrast", Side.LEFT, () -> getToggleButtonText(new TranslationTextComponent("cold_sweat.config.high_contrast.name"), ConfigSettings.HIGH_CONTRAST.get()),
                button -> ConfigSettings.HIGH_CONTRAST.set(!ConfigSettings.HIGH_CONTRAST.get()),
                false, false, true, new TranslationTextComponent("cold_sweat.config.high_contrast.desc"));

        // Show Water Effect
        this.addButton("show_water_effect", Side.LEFT, () -> getEnumButtonText(new TranslationTextComponent("cold_sweat.config.show_water_effect.name"), ConfigSettings.WATER_EFFECT_SETTING.get()),
                button -> ConfigSettings.WATER_EFFECT_SETTING.set(getNextCycle(ConfigSettings.WATER_EFFECT_SETTING.get())),
                false, false, true, new TranslationTextComponent("cold_sweat.config.show_water_effect.desc"));

        // Water Droplet Scale
        this.addSliderButton("water_droplet_scale", Side.LEFT,
                             () -> getSliderText(new TranslationTextComponent("cold_sweat.config.water_droplet_scale.name"), ConfigSettings.WATER_DROPLET_SCALE.get().min(), 5, 100, -1),
                             5, 100,
                             (value, button) -> ConfigSettings.WATER_DROPLET_SCALE.set(new IntegerBounds(value.intValue(), (int) (value * 1.2))),
                             button -> button.setValue(CSMath.blend(0, 1, ConfigSettings.WATER_DROPLET_SCALE.get().min(), 5, 100)),
                             false, true, new TranslationTextComponent("cold_sweat.config.water_droplet_scale.desc"));

        // Animate Soulspring Lamp
        this.addButton("animate_soulspring_lamp", Side.LEFT,
                       () -> getToggleButtonText(new TranslationTextComponent("cold_sweat.config.animate_soulspring_lamp.name"), ConfigSettings.ANIMATED_SOULSPRING_LAMP_MODEL.get()),
                       button ->
                       {
                           DynamicHolder<Boolean> setting = ConfigSettings.ANIMATED_SOULSPRING_LAMP_MODEL;
                           setting.set(!setting.get());
                           if (!setting.get())
                           {    ItemModelsProperties.register(ModItems.SOULSPRING_LAMP, new ResourceLocation(ColdSweat.MOD_ID, "soulspring_state"), RegisterItemOverrides.SOULSPRING_LAMP_PROPERTIES);
                           }
                           else RegisterItemOverrides.unregister(ModItems.SOULSPRING_LAMP);
                       },
                       false, false, true, new TranslationTextComponent("cold_sweat.config.animate_soulspring_lamp.desc"));

        // Config Button Repositioning Screen
        this.addButton("button_position", Side.LEFT, () -> new TranslationTextComponent("cold_sweat.config.config_button_pos.name"),
                       button ->
                       {
                           DrawConfigButton.EDIT_MODE = true;
                           this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
                       },
                       false, false, true, new TranslationTextComponent("cold_sweat.config.config_button_pos.desc"));

        // Direction Buttons: Body Temp Icon
        this.addDirectionPanel("icon_directions", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.temp_icon_location.name"),
                amount -> ConfigSettings.BODY_ICON_POS.set(new Vec2i(ConfigSettings.BODY_ICON_POS.get().x() + amount * ConfigScreen.SHIFT_AMOUNT.get(),
                                                                   ConfigSettings.BODY_ICON_POS.get().y())),
                amount -> ConfigSettings.BODY_ICON_POS.set(new Vec2i(ConfigSettings.BODY_ICON_POS.get().x(),
                                                                     ConfigSettings.BODY_ICON_POS.get().y() + amount * ConfigScreen.SHIFT_AMOUNT.get())),
                () ->
                {   ConfigSettings.BODY_ICON_POS.set(new Vec2i(0, 0));
                },
                () ->
                {   ConfigSettings.BODY_ICON_ENABLED.set(!ConfigSettings.BODY_ICON_ENABLED.get());
                    return ConfigSettings.BODY_ICON_ENABLED.get();
                },
                false, false, true, true, new TranslationTextComponent("cold_sweat.config.temp_icon_location.desc"),
                                          new TranslationTextComponent("cold_sweat.config.offset_shift.name").withStyle(TextFormatting.GRAY));

        // Direction Buttons: Temp Readout
        this.addDirectionPanel("readout_directions", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.temp_readout_location.name"),
                amount -> ConfigSettings.BODY_READOUT_POS.set(new Vec2i(ConfigSettings.BODY_READOUT_POS.get().x() + amount * ConfigScreen.SHIFT_AMOUNT.get(),
                                                                        ConfigSettings.BODY_READOUT_POS.get().y())),
                amount -> ConfigSettings.BODY_READOUT_POS.set(new Vec2i(ConfigSettings.BODY_READOUT_POS.get().x(),
                                                                      ConfigSettings.BODY_READOUT_POS.get().y() + amount * ConfigScreen.SHIFT_AMOUNT.get())),
                () ->
                {   ConfigSettings.BODY_READOUT_POS.set(new Vec2i(0, 0));
                },
                () ->
                {   ConfigSettings.BODY_READOUT_ENABLED.set(!ConfigSettings.BODY_READOUT_ENABLED.get());
                    return ConfigSettings.BODY_READOUT_ENABLED.get();
                },
                false, false, true, true, new TranslationTextComponent("cold_sweat.config.temp_readout_location.desc"),
                                          new TranslationTextComponent("cold_sweat.config.offset_shift.name").withStyle(TextFormatting.GRAY));

        // Direction Buttons: World Temp Gauge
        this.addDirectionPanel("gauge_directions", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.world_temp_location.name"),
                amount -> ConfigSettings.WORLD_GAUGE_POS.set(new Vec2i(ConfigSettings.WORLD_GAUGE_POS.get().x() + amount * ConfigScreen.SHIFT_AMOUNT.get(),
                                                                          ConfigSettings.WORLD_GAUGE_POS.get().y())),
                amount -> ConfigSettings.WORLD_GAUGE_POS.set(new Vec2i(ConfigSettings.WORLD_GAUGE_POS.get().x(),
                                                                          ConfigSettings.WORLD_GAUGE_POS.get().y() + amount * ConfigScreen.SHIFT_AMOUNT.get())),
                () ->
                {   ConfigSettings.WORLD_GAUGE_POS.set(new Vec2i(0, 0));
                },
                () ->
                {   ConfigSettings.WORLD_GAUGE_ENABLED.set(!ConfigSettings.WORLD_GAUGE_ENABLED.get());
                    return ConfigSettings.WORLD_GAUGE_ENABLED.get();
                },
                false, false, true, true, new TranslationTextComponent("cold_sweat.config.world_temp_location.desc"),
                                          new TranslationTextComponent("cold_sweat.config.offset_shift.name").withStyle(TextFormatting.GRAY));

        // Direction Buttons: World Temp Gauge
        this.addDirectionPanel("food_effects_directions", Side.RIGHT, new TranslationTextComponent("cold_sweat.config.food_effects_location.name"),
                amount -> ConfigSettings.FOOD_EFFECTS_POS.set(new Vec2i(ConfigSettings.FOOD_EFFECTS_POS.get().x() + amount * ConfigScreen.SHIFT_AMOUNT.get(),
                                                                          ConfigSettings.FOOD_EFFECTS_POS.get().y())),
                amount -> ConfigSettings.FOOD_EFFECTS_POS.set(new Vec2i(ConfigSettings.FOOD_EFFECTS_POS.get().x(),
                                                                          ConfigSettings.FOOD_EFFECTS_POS.get().y() + amount * ConfigScreen.SHIFT_AMOUNT.get())),
                () ->
                {   ConfigSettings.FOOD_EFFECTS_POS.set(new Vec2i(0, 0));
                },
                () ->
                {   ConfigSettings.FOOD_EFFECTS_ENABLED.set(!ConfigSettings.FOOD_EFFECTS_ENABLED.get());
                    return ConfigSettings.FOOD_EFFECTS_ENABLED.get();
                },
                false, false, true, true, new TranslationTextComponent("cold_sweat.config.food_effects_location.desc"),
                                          new TranslationTextComponent("cold_sweat.config.offset_shift.name").withStyle(TextFormatting.GRAY));

        // Custom Hotbar
        this.addButton("custom_hotbar", Side.RIGHT, () -> getToggleButtonText(new TranslationTextComponent("cold_sweat.config.custom_hotbar.name"), ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get()),
                button -> ConfigSettings.CUSTOM_HOTBAR_LAYOUT.set(!ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get()),
                false, false, true, new TranslationTextComponent("cold_sweat.config.custom_hotbar.desc"));

        // Icon Bobbing
        this.addButton("icon_bobbing", Side.RIGHT, () -> getToggleButtonText(new TranslationTextComponent("cold_sweat.config.icon_bobbing.name"), ConfigSettings.ICON_BOBBING.get()),
                button -> ConfigSettings.ICON_BOBBING.set(!ConfigSettings.ICON_BOBBING.get()),
                false, false, true, new TranslationTextComponent("cold_sweat.config.icon_bobbing.desc"));

        // Icon Bobbing
        this.addButton("icon_bobbing", Side.RIGHT, () -> getToggleButtonText(new TranslationTextComponent("cold_sweat.config.icon_bobbing.name"), ConfigSettings.ICON_BOBBING.get()),
                       button -> ConfigSettings.ICON_BOBBING.set(!ConfigSettings.ICON_BOBBING.get()),
                       false, false, true, new TranslationTextComponent("cold_sweat.config.icon_bobbing.desc"));

        // Move body temp icon
        this.addButton("move_body_icon", Side.RIGHT, () -> getToggleButtonText(new TranslationTextComponent("cold_sweat.config.move_body_icon.name"), ConfigSettings.MOVE_BODY_ICON_WHEN_ADVANCED.get()),
                button -> ConfigSettings.MOVE_BODY_ICON_WHEN_ADVANCED.set(!ConfigSettings.MOVE_BODY_ICON_WHEN_ADVANCED.get()),
                false, false, true, new TranslationTextComponent("cold_sweat.config.move_body_icon.desc"));
    }

    @Override
    public void onClose()
    {
        super.onClose();
        ConfigScreen.saveConfig();
    }
}
